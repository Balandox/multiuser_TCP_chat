package org.suai.laba12.modelHelper;

import org.suai.laba12.model.ClientHandler;
import org.suai.laba12.model.Server;

import java.net.Socket;
import java.util.*;
import java.util.regex.Pattern;

public class KnockKnockProtocol {


    /**
     *
     * @param message
     *
     * @return "quit" if message == @quit;
     * "name" if message == @sendUser name someText;
     * "default" default message;
     * "User with this name doesn't exist!" if client input defunct user
     * "Incorrect @sendUser command format!" if client input incorrect @sendUser command
     * "Incorrect @alarm command format!" if client input incorrect @alarm command
     */

    public static String processInput(String message, Server server, String clientName) {

        StringTokenizer sendUserTokenizer = new StringTokenizer(message);
        String firstToken = null;

        StringTokenizer alarmTokenizer = new StringTokenizer(message);
        String alarmCommandToken = null;
        String alarmTimerToken = null;

        if(message.equals("@quit"))
            return "quit";

        if(sendUserTokenizer.countTokens() >= 2) {
            firstToken = sendUserTokenizer.nextToken();
            if (firstToken.equals("@sendUser")) {
                if (sendUserTokenizer.countTokens() >= 2) {
                    String nameToken = sendUserTokenizer.nextToken();

                    for (ClientHandler clientHandler : server.getClientList())
                        if (nameToken.equals(clientHandler.getClientName()))
                            return nameToken;

                    return "User with this name doesn't exist!";
                } else {
                    return "Incorrect @sendUser command format!";
                }
            }
        }


        if(alarmTokenizer.countTokens() == 2){
            alarmCommandToken = alarmTokenizer.nextToken();
            alarmTimerToken = alarmTokenizer.nextToken();

            if(alarmCommandToken.equals("@alarm") && checkTimeFormat(alarmTimerToken)){
                Timer timer = setTimer(alarmTimerToken, server, clientName);
                server.getTimerMap().remove(clientName);
                server.getTimerMap().put(clientName, timer);
                return "@alarm";
            }
            else{
                return "Incorrect @alarm command format!";
            }
        }

        return "default";

    }

    private static boolean checkTimeFormat(String timeString){
        String TIME_24_HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
        Pattern pattern = Pattern.compile(TIME_24_HOURS_PATTERN);
        return pattern.matcher(timeString).matches();
    }

    private static Timer setTimer(String time, Server server, String clientName){
        StringTokenizer timeTokenizer = new StringTokenizer(time, ":");
        String hour = timeTokenizer.nextToken();
        String minute = timeTokenizer.nextToken();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        cal.set(Calendar.MINUTE, Integer.parseInt(minute));
        Timer timer = new Timer();
        timer.schedule(new Alarm(server, clientName), cal.getTime());
        return timer;
    }


}
