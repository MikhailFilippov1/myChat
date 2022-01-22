package ru.geekbrains.myChat.myChat_Client;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class myChatClientController implements Initializable {

    static String currentContact = null;

    @FXML
    public VBox mainChatPanel;
    @FXML
    public TextArea mainChatArea;
    @FXML
    public ListView contactList;
    @FXML
    public TextField inputField;
    @FXML
    public Button btnSend;


    public void connectToServer(ActionEvent actionEvent) {
    }

    public void disconnectFromServer(ActionEvent actionEvent) {
    }

    public void mockAction(ActionEvent actionEvent) {
    }

    public void exit(ActionEvent actionEvent) {
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

       // var currentContact = contactList.getSelectionModel().getSelectedItem(); // Простой вариант
        mainChatArea.setWrapText(true);
        if(currentContact != null)mainChatArea.appendText("To " + currentContact +": " + message + System.lineSeparator());
        else mainChatArea.appendText("To all: " + message + System.lineSeparator());
        inputField.clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        var contacts = new ArrayList<>();
        contacts.add("Mom");
        contacts.add("Daddy");
        contacts.add("Causin");
        contacts.add("Bro");
        contacts.add("Sister");
        contacts.add("Friend");
        contacts.add("Enemy");
        contacts.add("Vinnie the Pooh");

        contactList.setItems(FXCollections.observableList(contacts));

        // Вариант посложнее, вдруг придется группы выделять и тд. Но нездорово, что в Initialize лежит, на мой взгляд

        MultipleSelectionModel<String> contactSelectionList = contactList.getSelectionModel();
        contactSelectionList.selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldContact, String newContact) {
                currentContact = newContact;
            }
        });

    }

}
