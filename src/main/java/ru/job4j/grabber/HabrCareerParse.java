package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGECOUNT = 5;

    public static void main(String[] args) throws IOException {
        HabrCareerParse desc = new HabrCareerParse();
        desc.retrieveDescription("https://career.habr.com/vacancies/1000143941");
        for (int pageNumber = 1; pageNumber <= PAGECOUNT; pageNumber++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                Element dateElement = row.select(".vacancy-card__date").first();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s %s%n", vacancyName, link, "дата размещения: ", dateElement.child(0).attr("datetime"));
                System.out.println(desc.retrieveDescription(link));
            });
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
}