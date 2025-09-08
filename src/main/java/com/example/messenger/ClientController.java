// ClientController.java
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    @FXML private Button button_send, button_image;
    @FXML private TextField tf_message, tf_username;
    @FXML private VBox vbox_messages;
    @FXML private ScrollPane sp_main;

    private Client client;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        button_send.setDisable(true);
        tf_message.setDisable(true);
        button_image.setDisable(true);

        tf_username.setOnAction(event -> connectWithUsername());

        vbox_messages.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                sp_main.setVvalue((Double) newValue);
            }
        });

        button_send.setOnAction(event -> {
            String messageToSend = tf_message.getText();
            if (!messageToSend.isEmpty()) {
                String fullMessage = tf_username.getText() + ": " + messageToSend;
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER_RIGHT);
                hBox.setPadding(new Insets(5, 5, 5, 10));

                Text text = new Text(fullMessage);
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
                    // Display locally
                    HBox hBox = new HBox();
                    hBox.setAlignment(Pos.CENTER_RIGHT);
                    hBox.setPadding(new Insets(5, 5, 5, 10));

                    Image image = new Image(new ByteArrayInputStream(imageBytes));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(200);
                    imageView.setPreserveRatio(true);
                    Text text = new Text(tf_username.getText() + ": ");
                    hBox.getChildren().addAll(text, imageView);
                    vbox_messages.getChildren().add(hBox);

                    // Send to server
                    client.sendImageToServer(imageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void connectWithUsername() {
        String username = tf_username.getText();
        if (!username.isEmpty()) {
            try {
                client = new Client(new Socket("localhost", 1234), username);
                client.receiveMessageFromServer(vbox_messages);
                tf_username.setDisable(true);
                button_send.setDisable(false);
                tf_message.setDisable(false);
                button_image.setDisable(false);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error creating client in ClientController.java");
            }
        }
    }

    public static void addLabel(String messageFromServer, VBox vbox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(messageFromServer);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: rgb(233,233,235); -fx-background-radius: 20px;");
        textFlow.setPadding(new Insets(5, 10, 5, 10));

        hBox.getChildren().add(textFlow);

        Platform.runLater(() -> vbox.getChildren().add(hBox));
    }
}