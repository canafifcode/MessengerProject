package com.example.messenger;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.swing.text.Element;
import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error initializing client.");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessageToServer(String messageToServer) {
        try {
            bufferedWriter.write(messageToServer);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error sending message to server.");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void receiveMessageFromServer(VBox vBox) {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    String messageFromServer = bufferedReader.readLine();
                    if (messageFromServer.startsWith("IMAGE:")) {
                        int size = Integer.parseInt(messageFromServer.substring(6));
                        byte[] imageData = new byte[size];
                        InputStream is = socket.getInputStream();
                        int bytesRead = 0;
                        while (bytesRead < size) {
                            int read = is.read(imageData, bytesRead, size - bytesRead);
                            if (read == -1) break;
                            bytesRead += read;
                        }
                        Platform.runLater(() -> {
                            Image image = new Image(new ByteArrayInputStream(imageData));
                            javafx.scene.image.ImageView imageView= new ImageView(image);
                            imageView.setFitWidth(200);
                            imageView.setPreserveRatio(true);
                            HBox hbox= new HBox(imageView);
                            hbox.setAlignment(Pos.CENTER_LEFT);
                            vBox.getChildren().add(hbox);
                        });
                    } else {
                        ClientController.addLabel(messageFromServer, vBox);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error receiving message from server.");
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }
}