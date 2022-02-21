package ru.geekbrains.myChat.chat_server;

import ru.geekbrains.myChat.chat_server.auth.InMemoryAuthService;
import ru.geekbrains.myChat.chat_server.server.MySimpleMulticlientServer;

public class App {

    public static void main(String[] args) {
        new MySimpleMulticlientServer(new InMemoryAuthService()).start();
    }
}
