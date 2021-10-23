package ru.rompet.cloudstorage.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticationService {
    public static boolean auth(String login, String password) {
        Connection connection = DBConnection.getConnection();
        String query = "SELECT * FROM credentials WHERE login = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch(SQLException e) {
            throw new RuntimeException("SWW", e);
        }
    }

    public static boolean register(String login, String password) {
        Connection connection = DBConnection.getConnection();
        String query = "INSERT INTO credentials (`login`, `password`) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, login);
            statement.setString(2, password);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
            // add logging
        }
    }
}
