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

        String[] rcv = new String(m.getMessageContent()).replaceAll(""+PeerMessage.PADDING_SYMBOL, "").split(":");

        int fileSize = Integer.parseInt(rcv[1]);
        String filename = rcv[0];

        try {

            InputStream is = c.getIs();
            FileOutputStream fos = new FileOutputStream(filename);

            int read = 0;
            int totalRead = 0;
            int remaining = fileSize;

            byte[] buffer = new byte[4032];

            while((read = is.read(buffer, 0, Math.min(buffer.length, remaining))) != -1 && remaining != 0){
                //on ajuste la taille du content lu
                //read -= PeerMessage.HEADER_SIZE;
                totalRead += read;
                remaining -= read;
                System.out.println("read " + totalRead + " bytes");
                fos.write(buffer,0,read);//new PeerMessage(buffer).getMessageContent());
                //buffer = new byte[Math.min(4032, remaining)];
            }
            fos.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {}
    }
}
