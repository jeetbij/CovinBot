package com.example.cowin.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Optional;

import com.example.cowin.model.Pincode;
import com.example.cowin.model.Subscription;
import com.example.cowin.model.User;
import com.example.cowin.service.BeanUtilService;
import com.example.cowin.service.IPincodeService;
import com.example.cowin.service.ISubscribeService;
import com.example.cowin.service.IUserService;
import com.example.cowin.service.PincodeService;
import com.example.cowin.service.SubscribeService;
import com.example.cowin.service.UserService;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramCowinBot extends TelegramLongPollingBot {
    
    private final String TELEGRAM_STRING = "TELEGRAM";

    //Bot information
    // private final String CRYPTO_BOT_TOKEN = "1729919631:AAExAz2WRfbmWMFsNzRfBZBsHxXD9KDlCMc"; // Test
    private final String CRYPTO_BOT_TOKEN = "1893229099:AAHzF6__N3Jn_hwDUibAxIdpHUK4QA5iIYI";
    private final String CRYPTO_BOT_NAME = "CryptoBot";

    //Commands
    private final String START_COMMAND = "start";
    private final String HELP_COMMAND = "help";
    private final String SUBSCRIBE_COMMAND = "notify";
    private final String UNSUBSCRIBE_COMMAND = "stop";
    private final String LIST_SUBSCRIPTION_COMMAND = "list";

    //Messages
    private final String START_MESSAGE = "Hi! This bot helps you find available vaccination slots in a provided pincode area.\n\nTo see what this bot can do type command /help.";
    private final String HELP_MESSAGE = "This bot helps you find available vaccination slots in provided pincode.\n\nYou can use below commands to control updates.\n\n1. /notify <pincode> <age> - To get a notification as soon as slots are available.\n\n2. /list - To list all notification requests registered by you.\n\n3. /stop <pincode> <age> - To stop getting notifications.";
    private final String UNRECOGNIZED_COMMAND = "Unrecognized command.";
    private final String SUBSCRIBED_MESSAGE = "Your request has been registered successfully. You will be notified when vaccine slots are available in area with pincode %s for %s year olds.";
    private final String ALREADY_SUBSCRIBED_MESSAGE = "You have already a request registered in area with pincode %s for %s year olds.";
    private final String UNSUBSCRIBED_MESSAGE = "You will not be notified anymore for vaccine slot availability in area with pincode %s for %s year olds.";
    private final String NO_SUBSCRIPTION_MESSAGE = "You don't have any registered notification requests.";
    private final String NO_SUBSCRIPTION_FOUND = "No request found for pincode %s and age %s.";
    private final String INVALID_PINCODE = "Invalid pincode - %s";
    private final String INVALID_MESSAGE = "Invalid input.";

    //Repositories
    private ISubscribeService subscribeService = BeanUtilService.getBean(SubscribeService.class);
    private IUserService userService = BeanUtilService.getBean(UserService.class);
    private IPincodeService pincodeService = BeanUtilService.getBean(PincodeService.class);

    
    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage reply = new SendMessage(); // Create a SendMessage object with mandatory fields
            String chatId = update.getMessage().getChatId().toString();
            reply.setChatId(chatId);
            // message.setText(update.getMessage().getText());

            String userId = update.getMessage().getChat().getId().toString();
            String firstName = update.getMessage().getChat().getFirstName();
            String lastName = update.getMessage().getChat().getLastName();
            String channel = TELEGRAM_STRING;

            User user = getUser(userId, channel);
            if(user == null) {
                user = createUser(userId, firstName, lastName, channel, chatId);
            }

            String message = update.getMessage().getText();
            String[] splittedMessage = message.split(" ");

            String command = getCommand(message);

            switch (command) {
            case START_COMMAND:
                createReplyMessage(reply, START_MESSAGE);
                break;

            case SUBSCRIBE_COMMAND:
                if (splittedMessage.length > 1) {
                    if (splittedMessage[1].length() != 6) {
                        createReplyMessage(reply, String.format(INVALID_PINCODE, splittedMessage[1]));
                        break;
                    }
                    String pincode = getPincode(splittedMessage[1], userId);
                    Integer age = getAge(splittedMessage[2]);
                    if (pincode == null || age == null) {
                        createReplyMessage(reply, String.format(INVALID_MESSAGE, splittedMessage[1]));
                        break;
                    }
                    Boolean subscribed = subscribe(user, pincode, age);
                    if (subscribed) {
                        createReplyMessage(reply, String.format(SUBSCRIBED_MESSAGE, pincode, age));
                    } else {
                        createReplyMessage(reply, String.format(ALREADY_SUBSCRIBED_MESSAGE, pincode, age));
                    }
                } else {
                    createReplyMessage(reply, INVALID_MESSAGE);
                }
                break;

            case UNSUBSCRIBE_COMMAND:
                if (splittedMessage.length > 1) {
                    if (splittedMessage[1].length() != 6) {
                        createReplyMessage(reply, String.format(INVALID_PINCODE, splittedMessage[1]));
                        break;
                    }
                    String pincode = getPincode(splittedMessage[1], userId);
                    Integer age = getAge(splittedMessage[2]);
                    if (pincode == null || age == null) {
                        createReplyMessage(reply, String.format(INVALID_MESSAGE, String.format("%s %s", splittedMessage[1], splittedMessage[2])));
                        break;
                    }
                    Boolean unsubscribed = unsubscribe(user, pincode, age);
                    if (unsubscribed) {
                        createReplyMessage(reply, String.format(UNSUBSCRIBED_MESSAGE, pincode, age));
                    } else {
                        createReplyMessage(reply, String.format(NO_SUBSCRIPTION_FOUND, pincode, age));
                    }
                } else {
                    createReplyMessage(reply, INVALID_MESSAGE);
                }
                break;

            case LIST_SUBSCRIPTION_COMMAND:
                List<String> subscriptionList = getAllSubscription(user);
                String replyMessString = "";
                if (!(subscriptionList.size() > 0)) {
                    replyMessString = NO_SUBSCRIPTION_MESSAGE;
                }
                int i = 1;
                for (String string : subscriptionList) {
                    replyMessString += String.format("%s. %s\n", i, string);
                    i++;
                }
                createReplyMessage(reply, replyMessString);
                break;

            case HELP_COMMAND:
                createReplyMessage(reply, HELP_MESSAGE);
                break;

            default:
                createReplyMessage(reply, UNRECOGNIZED_COMMAND);
            }
            sendMessage(reply);
        }
    }

    @Override
    public String getBotUsername() {
        return CRYPTO_BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return CRYPTO_BOT_TOKEN;
    }

    private SendMessage createReplyMessage(SendMessage replyMessage, String text) {
        replyMessage.setText(text);
        return replyMessage;
    }

    private String getCommand(String message) {
        String[] messages = message.split(" ");
        String command = messages[0].replaceAll("/", "");
        return command;
    }

    public void sendMessage(SendMessage message) {
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private User getUser(String userId, String channel) {

        Optional<User> user = userService.findByUserId(userId, channel);
        if (user.isPresent()) {
            return user.get();
        }
        return null;

    }

    private User createUser(String userId, String firstName, String lastName, String channel, String chatId) {
        
        User user = new User();
        user.setUserId(userId);
        user.setFullName(getFullName(firstName, lastName));
        user.setChannel(channel);
        user.setChatId(chatId);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userService.save(user);
        return user;

    }

    private String getFullName(String firstName, String lastName) {

        return String.format("%s %s", firstName, lastName);

    }

    private Boolean subscribe(User user, String pincode, Integer age) {
        
        Subscription sb = new Subscription();
        Optional<Subscription> sbo =  subscribeService.findByNamePincodeAge(user, pincode, age);

        if (sbo.isPresent()) {
            return false;
        }
        sb.setUser(user);
        sb.setAge(age);
        sb.setPincode(pincode);
        sb.setCreatedAt(LocalDateTime.now());
        sb.setUpdatedAt(LocalDateTime.now());
        subscribeService.save(sb);

        return true;
    }

    private Boolean unsubscribe(User user, String pincode, Integer age) {
        
        Optional<Subscription> sbo =  subscribeService.findByNamePincodeAge(user, pincode, age);
        if (sbo.isPresent()) {
            Subscription sb = sbo.get();
            sb.setIsActive(false);
            sb.setUpdatedAt(LocalDateTime.now());
            subscribeService.save(sb);
            return true;
        }
        return false;
    }

    private List<String> getAllSubscription(User user) {

        List<Subscription> scList = subscribeService.findByName(user);
        List<String> requests = new ArrayList<>(); 
        for (Subscription subscription : scList) {
            requests.add(String.format("Pincode: %s, Age: %s", subscription.getPincode(), subscription.getAge()));
        }
        return requests;

    }

    private String getPincode(String pincode, String userId) {

        Optional<Pincode> po = pincodeService.findByPincode(pincode.strip(), userId);
        Pincode p = new Pincode();
        if (!po.isPresent()) {
            p.setPincode(pincode);
            p.setUserId(userId);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            pincodeService.save(p);
        } else {
            p = po.get();
        }
        return p.getPincode();
    }

    private Integer getAge(String age) {
        return Integer.valueOf(age.strip());
    }

}
