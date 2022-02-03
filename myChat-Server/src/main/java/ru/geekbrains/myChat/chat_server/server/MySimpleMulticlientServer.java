package ru.geekbrains.myChat.chat_server.server;

import ru.geekbrains.myChat.chat_server.auth.AuthService;
import ru.geekbrains.myChat.chat_server.auth.InMemoryAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MySimpleMulticlientServer {

    public static final String REGEX = "%!%";
    private static final int PORT = 8181;
    private AuthService authService;
    private List<ClientHandler> clientHandlers;

    public MySimpleMulticlientServer(AuthService authService){

        this.clientHandlers = new ArrayList<>();
        this.authService = authService;
    }

    public void start() {

        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started.");

            while (true){
                System.out.println("Waiting for connection ...");
                Socket socket = serverSocket.accept();
                System.out.printf("Client connecting.");
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clientHandler.handlerMethod();
                }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            authService.stop();
            shutdown();
        }
    }

    public void privateMessage(String from, String message){
        var splitMessage = message.split(REGEX);            // Код от 03.02.2022
        var mess = "/w" + REGEX + from + REGEX + splitMessage[1];            // Код от 03.02.2022
        for (ClientHandler clientHandler : clientHandlers) {        // Код от 03.02.2022
            if(clientHandler.getUserNick().equals(splitMessage[0])) { // Код от 03.02.2022
                clientHandler.send(mess);                               // Код от 03.02.2022
            }
        }
        for (ClientHandler clientHandler : clientHandlers) {        // Код от 03.02.2022
            if (clientHandler.getUserNick().equals(from)) {          // Код от 03.02.2022
                clientHandler.send(mess);                           // Код от 03.02.2022
            }
        }
    }

    public void broadcastMessage(String from, String message) {
        message = "/broadcast" + REGEX + from + REGEX + message;
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.send(message);
        }
    }

    public void shutdown(){

    }

    public synchronized void addAuthorizedClientToList(ClientHandler clientHandler){
        clientHandlers.add(clientHandler);
        sendOnlineClients();
    }

    public synchronized void removeAuthorizedClientToList(ClientHandler clientHandler){
        clientHandlers.remove(clientHandler);
        sendOnlineClients();
    }

    public void sendOnlineClients(){
        var sb = new StringBuilder("/list");
        sb.append(REGEX);
        for (ClientHandler clientHandler: clientHandlers) {
            sb.append(clientHandler.getUserNick());
            sb.append(REGEX);
        }
        var message = sb.toString();
        for (ClientHandler clientHandler: clientHandlers) {
            clientHandler.send(message);
        }
    }

    public synchronized boolean isNickBusy(String nick){
        for (ClientHandler clientHandler : clientHandlers) {
            if(clientHandler.getUserNick().equals(nick)){
                return true;
            }
        }
        return false;
    }

    public AuthService getAuthService() {
        return authService;
    }
}
