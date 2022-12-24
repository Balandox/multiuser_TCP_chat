package org.suai.laba12.modelHelper;

import org.suai.laba12.model.Server;

import java.util.Timer;
import java.util.TimerTask;

public class Alarm extends TimerTask {

    private Server server;
    private String clientName;

    public Alarm(Server server, String clientName){
        this.server = server;
        this.clientName = clientName;
    }

    @Override
    public void run() {
        this.server.sendMessageToOneClient("WAAAKE UUUUPPPP!!!!!!",this.server.findClientHandler(clientName));
    }

}
