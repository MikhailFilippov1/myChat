package ru.geekbrains.myChat.chat_server.server;

import ru.geekbrains.myChat.chat_server.error.WrongCredentialsException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private Thread handlerThread;
    private MySimpleMulticlientServer server;
    private String user;
    private Thread checkAuthThread;

    public ClientHandler(Socket socket, MySimpleMulticlientServer server) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Handler created");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handlerMethod() {
        handlerThread = new Thread(() -> {
            try {
                authorize();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted() && socket.isConnected()){
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
        switch (splitMessage[0]){
            case "/broadcast" :
                server.broadcastMessage(user, splitMessage[1]);
                break;
            case "/w" :
                var recepient = splitMessage[1];
                var mess = recepient + MySimpleMulticlientServer.REGEX + splitMessage[2];
                server.privateMessage(user, mess);
                break;
        }
    }

    private void authorize() throws InterruptedException, IOException {
        var authThread = new Thread();
        authThread.start();
        Thread.sleep(10000);
        while (true){
            try {
                var message = in.readUTF();
                if(message.startsWith("/auth")){
                    var parsedAuthMessage = message.split(MySimpleMulticlientServer.REGEX);
                    var response = "";      // Почему именно такое название, а не responce , например?
                    String nickName = null;
                    try {
                        nickName = server.getAuthService().authorizeUserByLoginAndPassword(parsedAuthMessage[1], parsedAuthMessage[2]);
                    } catch (WrongCredentialsException e) {
                        response = "/error" + MySimpleMulticlientServer.REGEX + e.getMessage();
                    }
                    if(server.isNickBusy(nickName)){
                        response = "/error" + MySimpleMulticlientServer.REGEX + "This client already connected";
                    }
                    if(!response.equals("")){
                        send(response);
                    } else {
                        this.user = nickName;
                        server.addAuthorizedClientToList(this);
                        send("/auth_ok" + MySimpleMulticlientServer.REGEX + nickName);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(Thread.currentThread().isAlive()) {
                authThread.interrupt();
                socket.close();
                handlerThread.interrupt();
                System.out.println("Time is delay... Client is disconnect...");
                break;
            }
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
