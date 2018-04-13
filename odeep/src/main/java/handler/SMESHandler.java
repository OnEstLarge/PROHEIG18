package handler;

import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;

public class SMESHandler implements MessageHandler{

    public void handleMessage(PeerConnection c, PeerMessage m){
        System.out.println("******************************************************************");
        System.out.println("**\n**INCOMING MESSAGE FROM " + m.getIdFrom() + "\n**\n**SAYING\n**");
        System.out.println("**   " + new String(m.getMessageContent()) + "\n**");
        System.out.println("******************************************************************");
    }
}
