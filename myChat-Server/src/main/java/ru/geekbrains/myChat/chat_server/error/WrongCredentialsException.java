package ru.geekbrains.myChat.chat_server.error;

public class WrongCredentialsException extends RuntimeException{

    public WrongCredentialsException() {
    }

    public WrongCredentialsException(String message) {
        super(message);
    }
}
