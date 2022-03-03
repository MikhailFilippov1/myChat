package ru.geekbrains.myChat.chat_server.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.myChat.chat_server.auth.AuthService;
import ru.geekbrains.myChat.chat_server.auth.InMemoryAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MySimpleMulticlientServer {

    public static final String REGEX = "%!%";
    private static final int PORT = 8181;
    private AuthService authService;
    private List<ClientHandler> clientHandlers;
    public static final String DB_CONNECTION_STRING = "jdbc:sqlite:db/users.db";
    private static final String CREATE_REQUEST = "create table if not exists users" +
            "(id integer primary key autoincrement, login text, pass text, nick text, secret text);";
    private static final String INSERT_REQUEST = "insert into users (login, pass, nick, secret) values ('log1', 'pass', 'Nick1', 'secret'), ('log2', 'pass', 'Nick2', 'secret'), ('log3', 'pass', 'Nick3', 'secret')";
    private static Connection connection;
    private static Statement statement;
    private static final Logger log = LogManager.getLogger();

    public MySimpleMulticlientServer(AuthService authService){

        this.clientHandlers = new ArrayList<>();
        this.authService = authService;
    }

    public void start() {

        ExecutorService cachedService = Executors.newCachedThreadPool();

        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
//            System.out.println("Server started.");
            log.info("Server started.");
            try {
                connectToDB();                  //Подключение к базе данных
//              createTableOfUsers();           //Создание таблицы пользователей
//              initializeTableOfUsers();       //Инициализация таблицы пользователей
            } catch (SQLException e){
                e.printStackTrace();
            }
            while (true){
//                System.out.println("Waiting for connection ...");
                log.info("Waiting for connection ...");
                Socket socket = serverSocket.accept();
//                System.out.printf("Client connecting.");
                log.info("Client connecting.");
                ClientHandler clientHandler = new ClientHandler(socket, this);
                cachedService.execute(clientHandler::handlerMethod);
                clientHandler.handlerMethod();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            authService.stop();
            cachedService.shutdown();
        }
    }

    private static void connectToDB() throws SQLException {
        connection = DriverManager.getConnection(DB_CONNECTION_STRING);
        statement = connection.createStatement();
    }

    private static void createTableOfUsers() throws SQLException{
        statement.execute(CREATE_REQUEST);
    }

    private static void initializeTableOfUsers() throws SQLException{
        statement.executeUpdate(INSERT_REQUEST);
    }

    public void privateMessage(String from, String message){
        var splitMessage = message.split(REGEX);            // Код от 03.02.2022
        var mess = "/w" + REGEX + from + REGEX + splitMessage[1];            // Код от 03.02.2022
        log.warn(message);
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
        log.warn(message);
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

    public void setAuthorizedClientToList(ClientHandler clientHandler) {
        var num = clientHandlers.indexOf(clientHandler);
        clientHandlers.set(num, clientHandler);
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
