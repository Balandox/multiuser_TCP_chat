package org.suai.laba12.model;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
короче че мы делаем
У нас будет мап имен клиентов и их активных таймеров(если такового нет, то null).
Когда будет заходить новый чел, то мы будем проверять заходил ли этот чел раньше и есть ли у него выставленный таймер
Будем проставлять булевскую переменную isBack
При создании нового ClientHandler будем проверять, что, если чел isBack == true && Timer != null, то будем присваивать в переменну Timer в классе ClientHandler
вот этот уже созданный таймер, иначе будильник можно будет поставить через processInput
 */

public class Server {

    private ExecutorService executorService;
    private List<ClientHandler> clientList;
    private int listeningPort;
    private Map<String, Timer> timerMap;

    private BufferedReader nameReader;

    private AtomicInteger clientCount;
    private static Lock clientListLock = new ReentrantLock();

    public Server(int listeningPort) {
        this.listeningPort = listeningPort;
        this.executorService = Executors.newCachedThreadPool();
        this.clientList = new ArrayList<>();
        this.clientCount = new AtomicInteger(0);
        this.timerMap = new HashMap<>();

        try{
            ServerSocket serverSocket = new ServerSocket(listeningPort);
            System.out.println("Server is running...");
            Socket clientSocket = null;

            while (true) {
                clientSocket = serverSocket.accept();

                this.nameReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String clientName = this.nameReader.readLine();
                //System.out.println(this.clientIsBack(clientName));

                ClientHandler client = new ClientHandler(clientSocket, this, clientName, this.clientIsBack(clientName));
                this.clientList.add(client);

                this.executorService.execute(client);
            }

        } catch (IOException exception) {
            System.out.println("Error when creating a server socket!!!");
            exception.printStackTrace();
            this.executorService.shutdown();
        }
    }

    public boolean clientIsBack(String clientName){
        for(Map.Entry<String, Timer> entry : this.timerMap.entrySet()){
            if(entry.getKey().equals(clientName))
                return true;
        }

        this.timerMap.put(clientName, null);
        return false;
    }



    public void sendMessageToAllClients(String msg) { // для отправления сообщений от сервака
        String finalMsg = "Server: " + msg;
        for (ClientHandler o : this.clientList) {
            o.sendMsg(finalMsg);
        }
    }

    public void sendMessageToAllClients(String msg, ClientHandler clientHandler) { // для отправления сообщений от пользователя
        for (ClientHandler o : this.clientList) {
            if(!o.equals(clientHandler))
                o.sendMsg(clientHandler.getClientName() + ": " + msg);
        }
    }

    public void sendMessageToOneClient(String msg, ClientHandler clientHandler){
        clientHandler.sendMsg(msg);
    }

    public void sendUserCommand(String msg, String nameOfClient){
        this.findClientHandler(nameOfClient).sendMsg(msg);
    }

    public ClientHandler findClientHandler(String nameOfClient){
        for(ClientHandler clientHandler : this.clientList)
            if(clientHandler.getClientName().equals(nameOfClient))
                return clientHandler;
        return null;
    }

    // удаляем клиента из коллекции при выходе из чата
    public void removeClient(ClientHandler client) {
        clientListLock.lock();
        try {
            this.clientList.remove(client);
        }
        finally {
            clientListLock.unlock();
        }
    }

    public AtomicInteger getClientCount() {
        return clientCount;
    }

    public Map<String, Timer> getTimerMap(){
        return this.timerMap;
    }

    public List<ClientHandler> getClientList() {
        clientListLock.lock();
        try {
            return clientList;
        }
        finally {
            clientListLock.unlock();
        }
    }
}


