package ru.job4j.grabber;

import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection connection;

    public PsqlStore(Properties config) {
        try {
            Class.forName(config.getProperty("driver-class-name"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps =
                     connection.prepareStatement(
                             "INSERT INTO post (name, text, link, created) "
                               + "VALUES (?, ?, ?, ?)"
                               + "ON CONFLICT(link)"
                               + "DO NOTHING",
                             Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getDescription());
            ps.setString(3, post.getLink());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.execute();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                post.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM post")) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                list.add(createPost(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Post findById(int id) {
        Post result = null;
        try (PreparedStatement ps =
                     connection.prepareStatement("SELECT * FROM post WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                result = createPost(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    private Post createPost(ResultSet resultSet) {
        try {
            return new Post(resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("text"),
                    resultSet.getString("link"),
                    resultSet.getTimestamp("created").toLocalDateTime());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Post post = new Post();
        post.setTitle("test insert on conflict post");
        post.setDescription("test desc post");
        post.setLink("https://ya.ru");
        post.setCreated(new HabrCareerDateTimeParser().parse("2024-06-07T13:41:13+01:00"));
        Properties config = new Properties();
        try (InputStream input = PsqlStore.class.getClassLoader()
                .getResourceAsStream("db/rabbit.properties")) {
            config.load(input);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        PsqlStore psqlS = new PsqlStore(config);
        psqlS.save(post);
        System.out.println(psqlS.getAll().toString());
        System.out.println(psqlS.findById(3).toString());
    }
}