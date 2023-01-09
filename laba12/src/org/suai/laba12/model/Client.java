
package org.suai.laba12.model;
import org.suai.laba12.modelHelper.KnockKnockProtocol;

import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Client {

    public static boolean checkIPv4(final String ip) {
        boolean isIPv4;

        if(Objects.equals(ip, "localhost"))
            return true;

        try {
            final InetAddress inet = InetAddress.getByName(ip);
            isIPv4 = inet.getHostAddress().equals(ip)
                    && inet instanceof Inet4Address;
        } catch (final UnknownHostException e) {
            isIPv4 = false;
        }

        return isIPv4;
    }

    public static void main(String[] args) throws IOException {
        InetAddress serverIp = null;

        if(checkIPv4(args[0])) {
            serverIp = InetAddress.getByName(args[0]);
        }
        else{
            System.out.println("Incorrect IP address!");
            System.exit(1);
        }

        int serverPort = 0;
        try {
            serverPort = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException exception){
            System.out.println("The port number must be an integer!");
            System.exit(1);
        }

        System.out.println("Welcome to multiuser chat application!");
        System.out.print("1. Set user name (@name Vasya)\n" +
                "2. Start a chatting with default name(@hello)\n" +
                "3. Exit (@quit)\nSelect action: ");
        Scanner console = new Scanner(System.in);
        String clientName = "";

        String choose = console.nextLine();

        switch (choose){
            case "@hello":
                clientName = "User1(Client)";
                break;
            case "@quit":
                System.out.println("Good bye!!!");
                System.exit(1);
                break;
            default:
                StringTokenizer stringTokenizer = new StringTokenizer(choose);
                if(Objects.equals(stringTokenizer.nextToken(), "@name")) {
                    clientName = stringTokenizer.nextToken();
                }
                else {
                    System.out.println("Incorrect input!");
                    System.exit(1);
                }
                break;
        }


        try{
            Socket clientSocket = new Socket(serverIp, serverPort);
            PrintWriter nameSender = new PrintWriter(clientSocket.getOutputStream(), true);
            nameSender.println(clientName);
            ListenerMessageThread listenerMessageThread = new ListenerMessageThread(clientSocket);
            SendMessageThread sendMessageThread = new SendMessageThread(clientSocket, clientName);

            ExecutorService executorService = Executors.newFixedThreadPool(2);
            executorService.execute(listenerMessageThread);
            executorService.execute(sendMessageThread);
        }
        catch (IOException exception){
            System.out.println("Error when creating a client socket!!!");
            exception.printStackTrace();
        }


    }

}


class SendMessageThread implements Runnable{

    private Socket clientSocket;
    private BufferedReader clientInput; // client message from console
    private PrintWriter out; // send clientInput to server
    private String clientName;

    public SendMessageThread(Socket clientSocket, String clientName) throws IOException {
        this.clientSocket = clientSocket;
        this.clientName = clientName;
        this.clientInput = new BufferedReader(new InputStreamReader(System.in));
        this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
    }

    public void run(){

        try {
            while (true) {

                String message = this.clientInput.readLine();
                this.out.println(message);

                if (message.equals("@quit")) {
                    System.out.println("Good Bye " + this.clientName);
                    this.out.close();
                    this.clientInput.close();
                    System.exit(1);
                }
            }
        }
            catch (IOException exception){
                System.out.println("Sending message error!");
                System.exit(1);
            }
        }
    }

class ListenerMessageThread implements Runnable{
    private Socket clientSocket;
    private BufferedReader in;

    public ListenerMessageThread(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
    }


    public void run(){

        try {
            while (true) {
                String fromServer = null;
                while ((fromServer = in.readLine()) != null) {
                    System.out.println(fromServer);
                }
            }
        }
        catch (IOException exception) {
            System.out.println("Receive message from server error! Server crashed:(");
            System.exit(1);
        }
        }

}
