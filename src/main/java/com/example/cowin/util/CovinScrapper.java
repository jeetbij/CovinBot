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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.ws.rs.core.UriBuilder;

import com.example.cowin.dto.CovinDataDTO;
import com.example.cowin.model.CovinData;
import com.example.cowin.model.Subscription;
import com.example.cowin.service.BeanUtilService;
import com.example.cowin.service.CovinDataService;
import com.example.cowin.service.ICovinDataService;
import com.example.cowin.service.ISubscribeService;
import com.example.cowin.service.SubscribeService;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class CovinScrapper {

    private final String MESSAGE_FORMAT = "You have slots available in pincode area %s, for %s year olds.\n\n%s\nVaccine - %s\n";

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

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(builder.build())
                .setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:87.0) Gecko/20100101 Firefox/87.0")
                .timeout(Duration.ofSeconds(5))
                .build();

        logger.info(String.format("Getting slots for pincode %s and date %s \n", pincode, date));

        HttpResponse<String> response = client
            .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {

            logger.info(String.format("Request failed with error %s, time - %s \n", response.body().toString(), LocalDateTime.now()));
        }

        return String.format("%s::%s", response.statusCode(), response.body().toString());
        
    }

    private void saveCovinResponse(String response) {

        CovinData covinData = new CovinData();
        covinData.setResponse(response);
        covinDataService.save(covinData);

    }

    private void checkSlotAvailability(String pincode, String date) {

        try {
            String res = getSlots(pincode, date);
            String[] splittedRes = res.split("::");
            String statusCode = splittedRes[0];
            String response = splittedRes[1];

            saveCovinResponse(response);

            Gson g = new Gson();
            CovinDataDTO cdd = g.fromJson(response, CovinDataDTO.class);
            
            for (CovinDataDTO.Center center : cdd.getCenters()) {
                for (CovinDataDTO.Center.Session session : center.getSessions()) {
                    String address = String.format("%s, %s, %s, %s, %s, %s", center.getCenterName(), center.getCenterAddress(), center.getBlockName(), center.getDistrictName(), center.getPincode(), center.getStateName());
                    
                    if (session.getAvailableCapacity() > 0) {
                        if (session.getMinAgeLimit() == 18) {
                            List<Subscription> subscribers = subscribeService.findByPincodeAge(pincode, 18, 45);
                            for (Subscription subscriber : subscribers) {
                                String message = String.format(MESSAGE_FORMAT, subscriber.getPincode(), subscriber.getAge(), address, session.getVaccine());
                                telegramNotify(message, subscriber.getUser().getChatId());
                            }
                        } else if (session.getMinAgeLimit() == 45) {
                            List<Subscription> subscribers = subscribeService.findByPincodeAge(pincode, 45, 120);
                            for (Subscription subscriber : subscribers) {
                                String message = String.format(MESSAGE_FORMAT, subscriber.getPincode(), subscriber.getAge(), address, session.getVaccine());
                                telegramNotify(message, subscriber.getUser().getChatId());
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

    private void telegramNotify(String message, String chatId) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        logger.info(String.format("Notifying user with chatId %s, message - %s \n", chatId, message));
        
        TelegramCowinBot tcb = new TelegramCowinBot();
        tcb.sendMessage(sendMessage);

    }


    private void checkAndNotify() {

        List<String> pincodes = subscribeService.findDistinctPincode();

        for (String pincode : pincodes) {
            
            LocalDateTime myDateObj = LocalDateTime.now();
            ZonedDateTime zonedDate = myDateObj.atZone(ZoneId.of("Asia/Kolkata"));
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            String today = zonedDate.format(myFormatObj);
            
            checkSlotAvailability(pincode, today);

        }

    }

    private class ScrapTimer extends TimerTask {

        @Override
        public void run() {
            
            try {
                checkAndNotify();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
        
    }

    public void scrapper() {

        Timer timer = new Timer();
        timer.schedule(new ScrapTimer(), 0, 60*1000); 

    }

}
