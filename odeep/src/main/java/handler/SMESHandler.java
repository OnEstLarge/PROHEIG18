package handler;

import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;
import util.CipherUtil;
import Node.FileSharingNode;

import java.io.IOException;

public class SMESHandler implements MessageHandler{

    public void handleMessage(FileSharingNode n, PeerConnection c, PeerMessage m){
        System.out.println("******************************************************************");
        System.out.println("**INCOMING MESSAGE FROM " + CipherUtil.erasePadding(m.getIdFrom(), PeerMessage.PADDING_START) + " SAYING\n**");
        System.out.println("**   " + new String(CipherUtil.erasePadding(m.getMessageContent(), PeerMessage.PADDING_START) + "\n**"));
        System.out.println("******************************************************************");


        //TODO: On gère si le message reçu fait plus de 4096byte, pour l'instant on affiche jsute la suite
        byte[] b = new byte[4096];
        try {
            while (c.getIs().read(b) != -1) {
                System.out.println(new String(b));
            }
        } catch(IOException e) {}
    }
}
