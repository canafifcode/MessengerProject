package com.example.messenger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    @FXML private Button button_send, button_image;
    @FXML private TextField tf_message;
    @FXML private VBox vbox_messages;
    @FXML private ScrollPane sp_main;

    private Client client;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            client = new Client(new Socket("localhost", 7564)); // Match server port
            client.receiveMessageFromServer(vbox_messages);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error creating client in ClientController.java");
        }

        // Auto-scroll to bottom when new messages are added
        vbox_messages.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                sp_main.setVvalue((Double) newValue);
            }
        });

        // Send button action
        button_send.setOnAction(event -> {
            String messageToSend = tf_message.getText();
            if (!messageToSend.isEmpty()) {
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER_RIGHT);
                hBox.setPadding(new Insets(5, 5, 5, 10));

                Text text = new Text(messageToSend);
                TextFlow textFlow = new TextFlow(text);
                textFlow.setStyle("-fx-color: rgb(239,242,255); -fx-background-color: rgb(15,125,242); -fx-background-radius: 20px;");
                textFlow.setPadding(new Insets(5, 10, 5, 10));
                text.setFill(Color.color(0.934, 0.945, 0.996));

                hBox.getChildren().add(textFlow);
                vbox_messages.getChildren().add(hBox);

                client.sendMessageToServer(messageToSend);
                tf_message.clear();
            }
        });

        button_image.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"));
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                try {
                    byte[] imageBytes = Files.readAllBytes(file.toPath());
                    // Send type prefix to distinguish from text
                    client.sendMessageToServer("IMAGE:" + imageBytes.length);
                    // Send bytes
                    OutputStream os = client.getSocket().getOutputStream();
                    os.write(imageBytes);
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void addLabel(String messageFromServer, VBox vbox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT); // Received messages on left
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(messageFromServer);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: rgb(233,233,235); -fx-background-radius: 20px;");
        textFlow.setPadding(new Insets(5, 10, 5, 10));

        hBox.getChildren().add(textFlow);

        Platform.runLater(() -> vbox.getChildren().add(hBox));
    }
}