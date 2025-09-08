package com.example.messenger;
//

// handles ui interactions//

//
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

import java.net.ServerSocket;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerController implements Initializable {
    @FXML private Button button_send;
    @FXML private TextField tf_message;
    @FXML private VBox vbox_messages;
    @FXML private ScrollPane sp_main;

    private Server server;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //will start our server here
        try{
            server= new Server(new ServerSocket(7564));
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Error creating server");
        }

        //making srollpane scroll to the bottom whenever the height of vbox
        //changes(to keep the latest content visible
        vbox_messages.heightProperty().addListener(new ChangeListener<Number>() {
            //this method will be called whenever the height of vbox changes
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                sp_main.setVvalue((Double) newValue);
            }
        });

        //receive messages from client
        server.receiveMessageFromClient(vbox_messages);

        button_send.setOnAction(actionEvent -> {
            //what to do when button is pressed
            String messageToSend= tf_message.getText();
            if(!messageToSend.isEmpty()){
                //hbox will hold the message bubble
                HBox hbox= new HBox();
                hbox.setAlignment(Pos.CENTER);
                hbox.setPadding(new Insets(5,5,5,10));

                //message bubble
                Text text= new Text(messageToSend);
                //textflow-> to hold the text
                TextFlow textFlow= new TextFlow(text);
                textFlow.setStyle("-fx-color: rgb(239,242,255); -fx-background-color: rgb(15,125,242); -fx-background-radius: 20px;");
                textFlow.setPadding(new Insets(5,10,5,10));
                text.setFill(Color.color(0.934, 0.945, 0.996));

                //the hbox will contain the textflow
                hbox.getChildren().add(textFlow);
                vbox_messages.getChildren().add(hbox);//add the hbox to vbox

                server.sendMessageToClient(messageToSend);//send message to client
                tf_message.clear();//clear the textfield after sending the message
            }
        });
    }



public static void addLabel(String messageFromClient, VBox vbox) {
        //addLabel will add a new label to the vbox
    HBox hBox = new HBox();
    hBox.setAlignment(Pos.CENTER_LEFT); //received messages on left
    hBox.setPadding(new Insets(5, 5, 5, 10));

    //message bubble
    Text text = new Text(messageFromClient);
    TextFlow textFlow = new TextFlow(text);
    textFlow.setStyle("-fx-background-color: rgb(233,233,235); -fx-background-radius: 20px;");
    textFlow.setPadding(new Insets(5, 10, 5, 10));

    hBox.getChildren().add(textFlow);

    Platform.runLater(() -> vbox.getChildren().add(hBox));//add the hbox to vbox on the JavaFX Application Thread
    }
}
