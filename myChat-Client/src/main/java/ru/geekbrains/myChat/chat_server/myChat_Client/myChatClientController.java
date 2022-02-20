package ru.geekbrains.myChat.chat_server.myChat_Client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ru.geekbrains.myChat.chat_server.myChat_Client.network.MessageProcessor;
import ru.geekbrains.myChat.chat_server.myChat_Client.network.NetworkService;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class myChatClientController implements Initializable, MessageProcessor {

    public static final String REGEX = "%!%";

    static String currentContact = "ALL";        //Код от 03.02.2022

    private String nick;
    private NetworkService networkService;

    @FXML
    public VBox loginPanel;
    @FXML
    public VBox changeNickPanel;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public VBox mainChatPanel;
    @FXML
    public TextArea mainChatArea;
    @FXML
    public ListView contactList;
    @FXML
    public TextField inputField;
    @FXML
    public TextField newNickField;
    @FXML
    public Button btnSend;


    public void connectToServer(ActionEvent actionEvent) {
    }

    public void disconnectFromServer(ActionEvent actionEvent) {
    }

    public void mockAction(ActionEvent actionEvent) {
    }

    public void exit(ActionEvent actionEvent) throws IOException {
        var message = "/exit";
        networkService.sendMessages(message);
        if(!networkService.isConnected()){
            try {
                networkService.disConnect();
            } catch (IOException e) {
                e.printStackTrace();
                showError(e.getMessage());
            }
        }
        System.exit(1);
    }

    public void showHelp(ActionEvent actionEvent) {
    }

    public void showAbout(ActionEvent actionEvent) {
    }

    public void sendMessage(ActionEvent actionEvent) {
        var message = inputField.getText();
        if(message.isBlank()){
            return;
        }

        mainChatArea.setWrapText(true);
        currentContact = (String) contactList.getSelectionModel().getSelectedItem(); // Код от 03/02/22
        if(currentContact.equals("ALL")){
//            mainChatArea.setStyle("-fx-font-size: 12px; -fx-highlight-fill: green; -fx-text-align: right");//Код от 03/02/22
            networkService.sendMessages("/broadcast" + REGEX + message);
        } else {
//            mainChatArea.setStyle("-fx-font-size: 25px; -fx-highlight-fill: green; -fx-text-fill: red; -fx-text-align: right");//Код от 03/02/22
            networkService.sendMessages("/w" + REGEX + currentContact + REGEX + message);   //Код от 03/02/22
        }
        inputField.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.networkService = new NetworkService(this); //Почему в параметрах this? Непонятно
    }

    @Override
    public void processMessage(String message) {
        Platform.runLater(() -> parseIncomingMessage(message));     //Вот это было непонятно
    }

    private void parseIncomingMessage(String message){
        var splitMessage = message.split(REGEX);
        switch (splitMessage[0]) {
            case "/auth_ok" -> {
                this.nick = splitMessage[1];
                reestablishChatToScreen();              // Восстанавливаем предыдущие сообщения в чате
                loginPanel.setVisible(false);
                mainChatPanel.setVisible(true);
            }
            case "/nick_ok" -> {
                renameChatHistoryFile(splitMessage[1]);
                if(splitMessage[1] != "")this.nick = splitMessage[1];
                changeNickPanel.setVisible(false);
                mainChatPanel.setVisible(true);
            }
            case "/error" -> {
                System.out.println("Got Error" + splitMessage[1]);
                showError(splitMessage[1]);
            }
            case "/list" -> {
                var contacts = new ArrayList<String>();
                contacts.add("ALL");
                for (int i = 1; i < splitMessage.length; i++) {
                    contacts.add(splitMessage[i]);
                }
                contactList.setItems(FXCollections.observableList(contacts));
                contactList.getSelectionModel().selectFirst();
            }
            case "/w" -> {
                mainChatArea.appendText(splitMessage[1] + " to [" + contactList.getSelectionModel().getSelectedItem() + "]> "
                        + splitMessage[2] + System.lineSeparator());
                sendToRepository(splitMessage[1] + " to [" + contactList.getSelectionModel().getSelectedItem() + "]> "
                        + splitMessage[2] + System.lineSeparator());
            }
            case "/broadcast" -> {
                mainChatArea.appendText(splitMessage[1] + " to ALL> " + splitMessage[2] + System.lineSeparator());
                sendToRepository(splitMessage[1] + " to ALL> " + splitMessage[2] + System.lineSeparator());
            }
        }
    }

    private void showError(String message) {
        var alert = new Alert(Alert.AlertType.ERROR,
                "An error occurred: " + message, ButtonType.OK);
        alert.showAndWait();
    }

    public void sendAuth(ActionEvent actionEvent) {
        var login = loginField.getText();
        var password = passwordField.getText();
        if(login.isBlank() || password.isBlank()){
            return;
        }

        var message = "/auth" + REGEX + login + REGEX + password;

        if(!networkService.isConnected()){
            try {
                networkService.connect();
            } catch (IOException e) {
                e.printStackTrace();
                showError(e.getMessage());
            }
        }
        networkService.sendMessages(message);
    }

   public void confirmNick(ActionEvent actionEvent) {
        var newNick = newNickField.getText();
        if(newNick == ""){
            changeNickPanel.setVisible(false);
            mainChatPanel.setVisible(true);
            return;
        }

       var message = "/nick" + REGEX + newNick;

       if(!networkService.isConnected()){
           try {
               networkService.connect();
           } catch (IOException e) {
               e.printStackTrace();
               showError(e.getMessage());
           }
       }
       networkService.sendMessages(message);
    }

    public void changeNickPanel(ActionEvent actionEvent) {
        changeNickPanel.setVisible(true);
        mainChatPanel.setVisible(false);
    }

    public void sendToRepository(String message){
        try (var os = new FileOutputStream(new File("chat_repository/" + nick + "_history.txt"), true);){
            os.write(message.getBytes(), 0, message.length());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void reestablishChatToScreen(){
        File file = new File("chat_repository/" + nick + "_history.txt");
        var lines = 0L;
        var numberOfLines = 100L;                // Количество строк прошлых сообщений к выводу на панель
        try (var reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) {
                lines++;                        // Определяем общее количество строк в предыдущих сообщениях
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (var reader = new BufferedReader(new FileReader(file))) {
            var s = "";
            while ((s = reader.readLine()) != null) {
                if(lines >= (lines - numberOfLines))
                    mainChatArea.appendText(s + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renameChatHistoryFile(String message){
        File old_file = new File("chat_repository/" + nick + "_history.txt");
        File new_file = new File("chat_repository/" + message + "_history.txt");

        try{
            if(!new_file.exists())new_file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(old_file);
            output = new FileOutputStream(new_file);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {output.close(); }
                if (input != null) { input.close();  }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        old_file.delete();
    }
}
