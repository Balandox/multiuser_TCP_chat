package org.suai.laba12.modelHelper;

import org.suai.laba12.model.Server;

public class ServerRunner {

    public static void main(String[] args) {

        int listeningPort = 0;
        try {
            listeningPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            System.out.println("The port number must be an integer!");
            System.exit(1);
        }

        Server server = new Server(listeningPort);

    }

}
