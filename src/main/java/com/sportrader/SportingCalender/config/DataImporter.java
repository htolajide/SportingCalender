package com.sportrader.SportingCalender.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportrader.SportingCalender.dto.JsonEventData;
import com.sportrader.SportingCalender.service.EventService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataImporter {

    @Bean
    public CommandLineRunner importJsonData(EventService eventService) {
        return args -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                InputStream inputStream = new ClassPathResource("DATA.json").getInputStream();
                
                // Parse JSON wrapper
                JsonDataWrapper wrapper = mapper.readValue(inputStream, JsonDataWrapper.class);
                List<JsonEventData> events = wrapper.getData();

                System.out.println("Importing " + events.size() + " events...");
                
                for (JsonEventData jsonEvent : events) {
                    try {
                        eventService.importEventFromJson(jsonEvent);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Skipping invalid event: " + e.getMessage());
                    }
                }
                System.out.println("✓ Data import completed successfully!");
                
            } catch (Exception e) {
                System.err.println("✗ Error importing JSON data: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    // Wrapper class for JSON structure: { "data": [ ... ] }
    @lombok.Getter
    @lombok.Setter
    public static class JsonDataWrapper {
        private List<JsonEventData> data;
    }
}