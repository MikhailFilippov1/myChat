package ru.geekbrains.myChat.chat_server.auth;

import ru.geekbrains.myChat.chat_server.entity.User;

import java.sql.SQLException;

public interface AuthService {

    void start();
    void stop();
    String authorizeUserByLoginAndPassword(String login, String password) throws SQLException;
    void changeNick(String login, String newNick) throws SQLException;
    User createNewUser(String login, String password, String nick);
    void deleteUser(String login, String pass);
    void changePassword(String login, String oldPassword, String newPassword);
    void resetPassword(String login, String newPassword, String secret);

}
