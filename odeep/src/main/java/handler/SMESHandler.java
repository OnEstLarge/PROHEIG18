package handler;

import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;

import java.io.IOException;

public class SMESHandler implements MessageHandler{

    public void handleMessage(PeerConnection c, PeerMessage m){
        System.out.println("******************************************************************");
        System.out.println("**INCOMING MESSAGE FROM " + m.getIdFrom() + " SAYING\n**");
        System.out.println("**   " + new String(m.getMessageContent()).replaceAll("" + PeerMessage.PADDING_SYMBOL, "") + "\n**");
        System.out.println("******************************************************************");

        byte[] b = new byte[4096];
        try {
            while (c.getIs().read(b) != -1) {
                System.out.println(new String(b));
            }
        } catch(IOException e) {}
    }
}
