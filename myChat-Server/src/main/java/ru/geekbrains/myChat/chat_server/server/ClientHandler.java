package ru.geekbrains.myChat.chat_server.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.myChat.chat_server.error.WrongCredentialsException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {

    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private Thread handlerThread;
    private MySimpleMulticlientServer server;
    private String user;
    private static final Logger log = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(Socket socket, MySimpleMulticlientServer server) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            log.trace("Handler created");
//            System.out.println("Handler created");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handlerMethod() {
        handlerThread = new Thread(() -> {

            authorize();

            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()){
                try{
                    var message = in.readUTF();
                    handleMessage(message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        handlerThread.start();
    }

    private void handleMessage(String message) {
        var splitMessage = message.split(MySimpleMulticlientServer.REGEX);
        switch (splitMessage[0]) {
            case "/broadcast":
                server.broadcastMessage(user, splitMessage[1]);
                break;
            case "/w":
                var recepient = splitMessage[1];
                var mess = recepient + MySimpleMulticlientServer.REGEX + splitMessage[2];
                server.privateMessage(user, mess);
                break;
            case "/nick":
                try {
                    server.getAuthService().changeNick(getUserNick(), splitMessage[1]);
                } catch (WrongCredentialsException | SQLException e) {
                    e.printStackTrace();
                }
                this.user = splitMessage[1];
                server.setAuthorizedClientToList(this);
                send("/nick_ok" + MySimpleMulticlientServer.REGEX + splitMessage[1]);
                break;
            case "/exit":
                try {
                    server.removeAuthorizedClientToList(this);
                    handlerThread.interrupt();
                }catch (RuntimeException e){
                    e.printStackTrace();
                }
        }
    }

    private void authorize() {
        var timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (user == null) {
                            send("/error" + MySimpleMulticlientServer.REGEX + "Authentication timeout!\nPlease, try again later!");
                            Thread.sleep(50);
                            socket.close();
                            handlerThread.interrupt();
                            System.out.println("Connection with client closed");
                        }
                    } catch (InterruptedException | IOException e) {
                        e.getStackTrace();
                    }
                }
            }, 100000);
            try {
                while (true) {
                    var message = in.readUTF();
                    if (message.startsWith("/auth")) {
                        var parsedAuthMessage = message.split(MySimpleMulticlientServer.REGEX);
                        var response = "";
                        String nickname = null;
                        try {
                            nickname = server.getAuthService().authorizeUserByLoginAndPassword(parsedAuthMessage[1], parsedAuthMessage[2]);
                        } catch (WrongCredentialsException | SQLException e) {
                            response = "/error" + MySimpleMulticlientServer.REGEX + e.getMessage();
                            System.out.println("Wrong credentials, nick " + parsedAuthMessage[1]);
                        }
                        if (server.isNickBusy(nickname)) {
                            response = "/error" + MySimpleMulticlientServer.REGEX + "this client already connected";
                            System.out.println("Nick busy " + nickname);
                        }
                        if (!response.equals("")) {
                            send(response);
                        } else {
                            this.user = nickname;
                            File file = new File("chat_repository/" + nickname + "_history.txt");
                            if(!file.exists())file.createNewFile();
                            server.addAuthorizedClientToList(this);
                            send("/auth_ok" + MySimpleMulticlientServer.REGEX + nickname);
                            break;
                        }
                    }else if(message.startsWith("/newUser")){
                        var parsedAuthMessage = message.split(MySimpleMulticlientServer.REGEX);
                            try {
                                server.getAuthService().createNewUserInDB(parsedAuthMessage[1], parsedAuthMessage[2], parsedAuthMessage[3]);
                            } catch (WrongCredentialsException | SQLException e) {
                                e.printStackTrace();
                            }
                            this.user = parsedAuthMessage[3];
                        File file = new File("chat_repository/" + parsedAuthMessage[3] + "_history.txt");
                        if(!file.exists())file.createNewFile();
                            server.addAuthorizedClientToList(this);
                            send("/newUser_ok" + MySimpleMulticlientServer.REGEX + parsedAuthMessage[3]);
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void send(String message) {
        try{
            out.writeUTF(message);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public Thread getHandlerThread(){ return handlerThread;}

    public String getUserNick() {
        return this.user;
    }
}
