package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class HabrCareerDateTimeParserTest {
    @Test
    void parse() {
        DateTimeParser parser = new HabrCareerDateTimeParser();
        LocalDateTime time = parser.parse("2024-06-07T13:41:13+01:00");
        assertThat(time.getYear()).isEqualTo(2024);
    }
}
