package com.example.cowin;

import com.example.cowin.util.CovinScrapper;
import com.example.cowin.util.TelegramCowinBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class CowinApplication {

	public static void main(String[] args) {
		SpringApplication.run(CowinApplication.class, args);

		try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramCowinBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

		// Scrapper
		CovinScrapper cs = new CovinScrapper();
		cs.scrapper();
	}

}
