package org.suai.laba12.model;

import org.suai.laba12.modelHelper.KnockKnockProtocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Timer;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable{

    private Socket clientSocket;
    private Server server;
    private String clientName;
    private PrintWriter out;
    private BufferedReader in;
    private Timer timer;

    private boolean clientIsBack;

    public ClientHandler(Socket clientSocket, Server server, String clientName, boolean clientIsBack){
        this.clientSocket = clientSocket;
        this.server = server;
        this.server.getClientCount().incrementAndGet();
        this.clientName = clientName;
        this.clientIsBack = clientIsBack;

        if(clientIsBack)
            this.timer = this.server.getTimerMap().get(clientName);
        else
            this.timer = null;

        try {
            this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        }
        catch (IOException exception){
            exception.printStackTrace();
        }
    }


    public void run(){

        this.server.sendMessageToAllClients("A new member(@" + clientName + ") has entered the chat room!");
        this.server.sendMessageToAllClients("Clients in chat = " + this.server.getClientCount());

        try {
                boolean quit = false;
                String clientMessage = null;
                while ((clientMessage = this.in.readLine()) != null) {
                    String actionString = KnockKnockProtocol.processInput(clientMessage, this.server, this.clientName);
                    switch (actionString) {
                        case "quit":
                            this.server.sendMessageToAllClients("@" + this.clientName + " has left from chat room:(");
                            this.server.sendMessageToAllClients("Amount of clients in the chat now = " + this.server.getClientCount().decrementAndGet());
                            this.server.removeClient(this);
                            this.clientSocket.close();
                            this.in.close();
                            this.out.close();
                            quit = true;
                            System.out.println("@" + this.clientName + " has left from the chat room!");
                            break;

                        case "default":
                            this.server.sendMessageToAllClients(clientMessage, this);
                            break;

                        case "@alarm": // успешно поставили будильник
                            this.server.sendMessageToOneClient("The alarm is successfully set", this);
                            this.timer = this.server.getTimerMap().get(this.clientName);
                            break;

                        case "Incorrect @alarm command format!":
                            this.server.sendMessageToOneClient("Server: Incorrect @alarm command format!\nRight format: @alarm <hh:mm>", this);
                            break;

                        case "User with this name doesn't exist!":
                            this.server.sendMessageToOneClient("Server: User with this name doesn't exist!", this);
                            break;

                        case "Incorrect @sendUser command format!":
                            this.server.sendMessageToOneClient("Server: Incorrect command format!\nRight format: @sendUser <UserName> <message>", this);
                            break;

                        default: // @sendUser
                            this.server.sendUserCommand(clientMessage, actionString);
                            break;
                    }
                    if(quit)
                        break;
                }
        }
        catch (IOException e) {
            System.out.println("@" + this.clientName + " crashed out of the chat room!");
            this.server.sendMessageToAllClients("@" + this.clientName + " has left from chat room:(");
            this.server.sendMessageToAllClients("Amount of clients in the chat now = " + this.server.getClientCount().decrementAndGet());
            this.server.removeClient(this);
            try {
                this.clientSocket.close();
            } catch (IOException ex) {
                System.out.println("Error when closing client Socket!");
            }
            try {
                this.in.close();
            } catch (IOException ex) {
                System.out.println("Error when closing client Stream!");
            }
            this.out.close();
        }

    }

    public void sendMsg(String msg) {
        try {
            out.println(msg);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getClientName() {
        return clientName;
    }


}
