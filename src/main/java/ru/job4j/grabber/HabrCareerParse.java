package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private final DateTimeParser dateTimeParser;
    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGECOUNT = 5;
    private static int tempIndex = 0;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse init = new HabrCareerParse(new HabrCareerDateTimeParser());
        for (int pageNumber = 1; pageNumber <= PAGECOUNT; pageNumber++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            System.out.println(init.list(fullLink).toString());
        }

    }

    private String retrieveDescription(String link)  {
        Connection connection = Jsoup.connect(link);
        Document document = null;
        try {
            document = connection.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Element row = document.select(".vacancy-description__text").first();
        StringBuilder desc = new StringBuilder();
        for (int r = 0; r < row.childNodeSize(); r++) {
            String[] strings = row.child(r)
                    .text()
                    .split(";");
            for (String string : strings) {
                desc.append(string)
                        .append("\n");
            }
        }
        return desc.toString();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> result = new ArrayList<>();
            Connection connection = Jsoup.connect(link);
            Document document = null;
            try {
                document = connection.get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                Element dateElement = row.select(".vacancy-card__date").first();
                LocalDateTime time = dateTimeParser.parse(dateElement.child(0).attr("datetime"));
                String url = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.println(this.retrieveDescription(url));
                result.add(new Post(tempIndex++, vacancyName, url, this.retrieveDescription(url), time));
            });
        return result;
    }
}