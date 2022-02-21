package ru.geekbrains.myChat.chat_server.myChat_Client.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NetworkService {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8181;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private MessageProcessor messageProcessor;

    public NetworkService(MessageProcessor messageProcessor)  {
        this.messageProcessor = messageProcessor;
    }

    public void connect() throws IOException {
        this.socket = new Socket(HOST, PORT);
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
        readMessages();
    }

    public void disConnect() throws IOException {
        this.socket.close();
        this.out.close();
        this.in.close();
        this.close();
    }

    public void readMessages(){
        Thread thread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()){
                    var message = in.readUTF();
                    messageProcessor.processMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void sendMessages(String message){
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected(){
        return socket != null && !socket.isClosed();
    }

    public void close(){
        try {
            if(socket != null && !socket.isClosed()){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
