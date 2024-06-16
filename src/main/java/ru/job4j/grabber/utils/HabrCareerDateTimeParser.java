package ru.job4j.grabber.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        return LocalDateTime.parse(parse, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static void main(String[] args) {
        DateTimeParser parser = new HabrCareerDateTimeParser();
        LocalDateTime time = parser.parse("2024-06-07T13:41:13");
        System.out.println("Year " + time.getYear());
    }
}