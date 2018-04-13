package handler;

import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SFILHandler  implements MessageHandler {

    public void handleMessage(PeerConnection c, PeerMessage m){

        //en vrai l eprmier message sera la taille du fichier
        System.out.println("Receiving " + new String(m.getMessageContent()).replaceAll(""+PeerMessage.PADDING_SYMBOL, ""));

        try {

            InputStream is = c.getIs();
            FileOutputStream fos = new FileOutputStream("rcv.txt");
            byte[] b = new byte[4096];
            while((is.read(b)) != -1){
                fos.write(b);
            }
            fos.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {}
    }
}
