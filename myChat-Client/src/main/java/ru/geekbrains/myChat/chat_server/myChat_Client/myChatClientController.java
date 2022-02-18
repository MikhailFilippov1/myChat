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

import java.io.IOException;
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
                loginPanel.setVisible(false);
                mainChatPanel.setVisible(true);
            }
            case "/nick_ok" -> {
                if(splitMessage[1] != "")this.nick = splitMessage[1];
                changeNickPanel.setVisible(false);
                mainChatPanel.setVisible(true);
            }
            case "/broadcast" -> mainChatArea.appendText(splitMessage[1] + " to ALL> " + splitMessage[2] + System.lineSeparator());
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
            case "/w" -> mainChatArea.appendText(splitMessage[1] + ">>> " + splitMessage[2] + System.lineSeparator());
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
}
