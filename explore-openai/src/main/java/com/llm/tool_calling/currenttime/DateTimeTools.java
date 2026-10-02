package com.llm.tool_calling.currenttime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class DateTimeTools {

    private static final Logger logger = LoggerFactory.getLogger(DateTimeTools.class);

    @Tool( description = "Get the current date and time in the users's timezone")
    public String getCurrentDateTimeWithoutZone() {
        logger.info("DateTimeTools is invoked - getCurrentDateTime");

        return LocalDateTime.now()
                .atZone(TimeZone.getDefault().toZoneId())
                .toString();
    }

    @Tool( description = "Get the current date and time in the specific timezone")
    public String getCurrentDateTime(String timeZone) {
        logger.info("DateTimeTools is invoked - getCurrentDateTime timeZone: {}", timeZone);

        try {
            ZoneId zoneId = ZoneId.of(timeZone);
            ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);

            return currentDateTime.toString();

//            return LocalDateTime.now()
//                    .atZone(TimeZone.getDefault().toZoneId())
//                    .toString();
        } catch (Exception e){
            logger.error("Invalid time zone provided: {}", timeZone, e);
            return "Invalid time zone provided: " + timeZone;
        }

    }
}
