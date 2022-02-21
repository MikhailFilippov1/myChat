package ru.geekbrains.myChat.chat_server.auth;

import ru.geekbrains.myChat.chat_server.entity.User;
import ru.geekbrains.myChat.chat_server.error.WrongCredentialsException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class fromDBAuthService implements AuthService{
    private static Connection connection;
    private static Statement statement;
    public static final String DB_CONNECTION_STRING = "jdbc:sqlite:db/users.db";

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public String authorizeUserByLoginAndPassword(String login, String password) throws SQLException {
        connection = DriverManager.getConnection(DB_CONNECTION_STRING);
        statement = connection.createStatement();
        try(var resultSet = statement.executeQuery("select * from users;")){    //
            while (resultSet.next()){
                if(login.equals(resultSet.getString("login")) && password.equals(resultSet.getString("pass"))){
                    return resultSet.getString("nick");
                }
            }
        }
        throw new WrongCredentialsException("Wrong login or password");
    }

    @Override
    public void changeNick(String oldNick, String newNick) throws SQLException {
        if(newNick == "")return;
        connection = DriverManager.getConnection(DB_CONNECTION_STRING);
        statement = connection.createStatement();
        var UPDATE_REQUEST = "update users set nick = '" + newNick +"' WHERE nick = '" + oldNick + "'";
        var count = statement.executeUpdate(UPDATE_REQUEST);
        System.out.printf("Updated %d row", count);
    }

    @Override
    public User createNewUser(String login, String password, String nick) {
        return null;
    }

    @Override
    public void deleteUser(String login, String pass) {

    }

    @Override
    public void changePassword(String login, String oldPassword, String newPassword) {

    }

    @Override
    public void resetPassword(String login, String newPassword, String secret) {

    }
}
