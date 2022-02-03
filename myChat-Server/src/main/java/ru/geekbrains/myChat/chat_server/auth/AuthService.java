package ru.geekbrains.myChat.chat_server.auth;

import ru.geekbrains.myChat.chat_server.entity.User;

public interface AuthService {

    void start();
    void stop();
    String authorizeUserByLoginAndPassword(String login, String password);
    String changeNick(String login, String newNick);
    User createNewUser(String login, String password, String nick);
    void deleteUser(String login, String pass);
    void changePassword(String login, String oldPassword, String newPassword);
    void resetPassword(String login, String newPassword, String secret);

}
