package com.example.messenger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Server {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public Server(ServerSocket serverSocket){
        try{
            this.socket= serverSocket.accept();
            this.bufferedReader= new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
        catch(Exception e){
            System.out.println("Error creating server in Serever.java");
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessageToClient(String messageToClient){
        try{
            bufferedWriter.write(messageToClient);//send message to client
            bufferedWriter.newLine();//add a new line after the message
            bufferedWriter.flush();//flush the stream
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending message to client in Server.java");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    public void receiveMessageFromClient(VBox vbox){
        new Thread(new Runnable(){
            @Override
            public void run() {
                while(socket.isConnected()){
                    try{
                        String messageFromClient= bufferedReader.readLine();
                        if(messageFromClient.startsWith("IMAGE:")){
                           int sizee= Integer.parseInt(messageFromClient.substring(6));
                           byte[] imageData= new byte[sizee];
                            InputStream is=socket.getInputStream();
                            is.read(imageData);

                            Platform.runLater(()->{
                                Image image = new Image(new ByteArrayInputStream(imageData));
                                ImageView imageView= new ImageView(image);
                                imageView.setFitWidth(200);
                                imageView.setPreserveRatio(true);
                                HBox hbox= new HBox(imageView);
                                hbox.setAlignment(Pos.CENTER_LEFT);
                                vbox.getChildren().add(hbox);
                            });
                        }
                        else {
                            ServerController.addLabel(messageFromClient, vbox);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Error receiving message from client in Server.java");
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        break;
                    }
                }
            }
        }).start();
    }
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try{
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
            if(socket!=null){
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
