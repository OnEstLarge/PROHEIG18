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

        int fileSize = Integer.parseInt(new String(m.getMessageContent()).replaceAll(""+PeerMessage.PADDING_SYMBOL, ""));
        System.out.println("Receiving: " + fileSize);

        try {

            InputStream is = c.getIs();
            FileOutputStream fos = new FileOutputStream("rcv.pdf");

            int read = 0;
            int totalRead = 0;
            int remaining = fileSize;

            byte[] buffer = new byte[4096];

            while((read = is.read(buffer, 0, Math.min(buffer.length, remaining)))  > 0){
                //on ajuste la taille du content lu
                read -= PeerMessage.HEADER_SIZE;
                totalRead += read;
                remaining -= read;
                System.out.println("read " + totalRead + " bytes");
                fos.write(new PeerMessage(buffer).getMessageContent());
            }
            fos.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {}
    }
}
