package com.example.cowin.util;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.UriBuilder;

import com.example.cowin.dto.CovinDataDTO;
import com.example.cowin.model.CovinData;
import com.example.cowin.model.Subscription;
import com.example.cowin.service.BeanUtilService;
import com.example.cowin.service.CovinDataService;
import com.example.cowin.service.ICovinDataService;
import com.example.cowin.service.ISubscribeService;
import com.example.cowin.service.SubscribeService;
import com.google.common.base.Optional;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class CovinScrapper {

    private final String MESSAGE_FORMAT = "You have slots available in pincode area %s, for %s year olds.\n\n%s\nFee Type - %s\nVaccine - %s\nAvailable Capacity - %s\n";

    private final List<String> userAgentList = new ArrayList<String>() {{
        add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
        add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36");
        add("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");
        add("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.76 Safari/537.36");
        add("Mozilla/5.0 (iPhone; CPU iPhone OS 13_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/80.0.3987.95 Mobile/15E148 Safari/604.1");
        add("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
        add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
        add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36");
        add("Mozilla/5.0 (Linux; Android 7.0; SM-G570M Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36");
        add("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36");
    }};
    private Integer SLEEP_TIME_IN_SECONDS = 30;

    Random rand = new Random();

    private ICovinDataService covinDataService = BeanUtilService.getBean(CovinDataService.class);
    private ISubscribeService subscribeService = BeanUtilService.getBean(SubscribeService.class);

    Logger logger = LoggerFactory.getLogger(CovinScrapper.class);

    private String getSlots(String pincode, String date) throws IOException, InterruptedException {
    
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_2)
                .build();

        UriBuilder builder = UriBuilder
                .fromUri("https://cdn-api.co-vin.in")
                .path("/api/v2/appointment/sessions/public/calendarByPin")
                .queryParam("pincode", pincode) // pincode=332023
                .queryParam("date", date); // date=13-05-2021

        String userAgent = userAgentList.get(rand.nextInt(10));

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(builder.build())
                .setHeader("User-Agent", userAgent)
                .timeout(Duration.ofSeconds(5))
                .build();

        logger.info(String.format("Getting slots for pincode %s and date %s --- user agent - %s\n", pincode, date, userAgent));

        HttpResponse<String> response = client
            .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {

            logger.info(String.format("Request failed with error %s, time - %s \n", response.body().toString(), LocalDateTime.now()));
            SLEEP_TIME_IN_SECONDS = 3*60;
            return response.body().toString();
        }
        
        SLEEP_TIME_IN_SECONDS = 30;
        return response.body().toString();
        
    }

    private void saveCovinResponse(String pincode, String date, String response) {

        CovinData covinData = new CovinData();
        covinData.setPincode(pincode);
        covinData.setDate(date);
        covinData.setResponse(response);
        covinDataService.save(covinData);

    }

    private void checkSlotAvailability(String pincode, String date) {

        try {
            String response = getSlots(pincode, date);
            saveCovinResponse(pincode, date, response);

            Gson g = new Gson();
            CovinDataDTO cdd = g.fromJson(response, CovinDataDTO.class);
            
            for (CovinDataDTO.Center center : cdd.getCenters()) {
                for (CovinDataDTO.Center.Session session : center.getSessions()) {
                    String address = String.format("%s, %s, %s, %s, %s, %s", center.getCenterName(), center.getCenterAddress(), center.getBlockName(), center.getDistrictName(), center.getPincode(), center.getStateName());
                    
                    if (session.getAvailableCapacity() == 0) {
                        if (session.getMinAgeLimit() == 18) {
                            List<Subscription> subscribers = subscribeService.findByPincodeAge(pincode, 18, 45);
                            for (Subscription subscriber : subscribers) {
                                String message = String.format(MESSAGE_FORMAT, subscriber.getPincode(), subscriber.getAge(), address, center.getFeeType(), session.getVaccine(), session.getAvailableCapacity());
                                telegramNotify(message, pincode, subscriber);
                            }
                        } else if (session.getMinAgeLimit() == 45) {
                            List<Subscription> subscribers = subscribeService.findByPincodeAge(pincode, 45, 120);
                            for (Subscription subscriber : subscribers) {
                                String message = String.format(MESSAGE_FORMAT, subscriber.getPincode(), subscriber.getAge(), address, center.getFeeType(), session.getVaccine(), session.getAvailableCapacity());
                                telegramNotify(message, pincode, subscriber);
                            }
                        }
                    }
                }   
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void telegramNotify(String message, String pincode, Subscription subscriber) {

        String chatId = subscriber.getUser().getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        LocalDateTime myDateObj = LocalDateTime.now();
        ZonedDateTime zonedDate = myDateObj.atZone(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date = zonedDate.format(myFormatObj);

        Optional<Subscription> subscription = subscribeService.findNotifiedSubscription(subscriber.getUser(), pincode, date);
        if (subscription.isPresent()) {
            logger.info(String.format("Not notifying user with chatId %s as user is already notified for pincode - %s. \n", chatId, pincode));
            return;
        }

        logger.info(String.format("Notifying user with chatId %s, message - %s \n", chatId, message));
        
        TelegramCowinBot tcb = new TelegramCowinBot();
        tcb.sendMessage(sendMessage);

        subscriber.setNotifiedOn(date);
        subscribeService.save(subscriber);

    }

    // private CovinData getLastCovinData(String pincode, String date) {

    //     Optional<CovinData> covinData = covinDataService.findLastRecordByPincodeDate(pincode, date);
    //     if (covinData.isPresent()) {
    //         Gson g = new Gson();
    //         CovinDataDTO cdd = g.fromJson(covinData.get().getResponse(), CovinDataDTO.class);

    //         if (cdd.getCenters().size() > 0) {
                
    //         }
    //     }

    // }


    private void checkAndNotify(List<String> pincodes) {

        for (String pincode : pincodes) {
            
            LocalDateTime myDateObj = LocalDateTime.now();
            ZonedDateTime zonedDate = myDateObj.atZone(ZoneId.of("Asia/Kolkata"));
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            String today = zonedDate.format(myFormatObj);


            checkSlotAvailability(pincode, today);

        }

    }

    private class ScrapThread implements Runnable {

        Thread scrapper;
        private String name;
        private List<String> pincodes;

        ScrapThread(String name, List<String> pincodes) {
            this.name = name;
            this.pincodes = pincodes;
        }

        @Override
        public void run() {
            
            try {
                checkAndNotify(pincodes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }

        public void start() {
            System.out.println(String.format("Thread %s started", name));
            if (scrapper == null) {
                scrapper = new Thread(this, name);
                scrapper.start();
            }
        }

    }

    public void scrapper() {

        // Timer timer = new Timer();
        // timer.schedule(new ScrapTimer(), 0, 60*1000); 

        while (true) {
            List<String> pincodes = subscribeService.findDistinctPincode();

            Integer length = pincodes.size();
            Integer portion = length/3;

            if(length > 3) {
                CovinScrapper.ScrapThread scrapper1 = new ScrapThread("Scrapper1", pincodes.subList(0, portion));
                CovinScrapper.ScrapThread scrapper2 = new ScrapThread("Scrapper2", pincodes.subList(portion, 2*portion));
                CovinScrapper.ScrapThread scrapper3 = new ScrapThread("Scrapper3", pincodes.subList(2*portion, length));

                scrapper1.start();
                scrapper2.start();
                scrapper3.start();
            } else {
                CovinScrapper.ScrapThread scrapper1 = new ScrapThread("Scrapper1", pincodes.subList(0, length));
                scrapper1.start();
            }

            try {
                System.out.println(String.format("Sleeping for %s secs", SLEEP_TIME_IN_SECONDS));
                TimeUnit.SECONDS.sleep(SLEEP_TIME_IN_SECONDS);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
