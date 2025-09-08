// Server.java
package com.example.messenger;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private VBox vbox; // Shared VBox for server GUI messages

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket);
                    clients.add(clientHandler);
                    clientHandler.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    closeServerSocket();
                }
            }
        }).start();
    }

    public void setVBox(VBox vbox) {
        this.vbox = vbox;
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
    }

    public void broadcastImage(byte[] imageData, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendImage(imageData);
                }
            }
        }
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        // Optional: Broadcast that the user left
        broadcastMessage(clientHandler.username + " has left the chat.", null);
        ServerController.addLabel(clientHandler.username + " has left the chat.", vbox);
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader bufferedReader;
        private BufferedWriter bufferedWriter;
        private String username;

        public ClientHandler(Socket socket) {
            try {
                this.socket = socket;
                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.username = bufferedReader.readLine();
                // Broadcast user joined
                broadcastMessage(username + " has joined the chat.", this);
                ServerController.addLabel(username + " has joined the chat.", vbox);
            } catch (IOException e) {
                e.printStackTrace();
                closeEverything();
            }
        }

        @Override
        public void run() {
            while (socket.isConnected()) {
                try {
                    String messageFromClient = bufferedReader.readLine();
                    if (messageFromClient == null) {
                        break;
                    }
                    if (messageFromClient.startsWith("IMAGE:")) {
                        int size = Integer.parseInt(messageFromClient.substring(6));
                        byte[] imageData = new byte[size];
                        InputStream is = socket.getInputStream();
                        int bytesRead = 0;
                        while (bytesRead < size) {
                            int read = is.read(imageData, bytesRead, size - bytesRead);
                            if (read == -1) break;
                            bytesRead += read;
                        }
                        // Display on server GUI
                        Platform.runLater(() -> {
                            Image image = new Image(new ByteArrayInputStream(imageData));
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(200);
                            imageView.setPreserveRatio(true);
                            HBox hBox = new HBox(imageView);
                            hBox.setAlignment(Pos.CENTER_LEFT);
                            Text text = new Text(username + ": ");
                            hBox.getChildren().add(0, text); // Add username
                            vbox.getChildren().add(hBox);
                        });
                        // Broadcast to other clients
                        broadcastImage(imageData, this);
                    } else {
                        String fullMessage = username + ": " + messageFromClient;
                        // Display on server GUI
                        ServerController.addLabel(fullMessage, vbox);
                        // Broadcast to other clients
                        broadcastMessage(fullMessage, this);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    closeEverything();
                    break;
                }
            }
        }

        public void sendMessage(String message) {
            try {
                bufferedWriter.write(message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                closeEverything();
            }
        }

        public void sendImage(byte[] imageData) {
            try {
                bufferedWriter.write("IMAGE:" + imageData.length);
                bufferedWriter.newLine();
                bufferedWriter.flush();
                OutputStream os = socket.getOutputStream();
                os.write(imageData);
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
                closeEverything();
            }
        }

        private void closeEverything() {
            removeClient(this);
            try {
                if (bufferedReader != null) bufferedReader.close();
                if (bufferedWriter != null) bufferedWriter.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}