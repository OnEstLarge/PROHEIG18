package handler;

import message.MessageHandler;
import message.MessageType;
import org.bouncycastle.crypto.InvalidCipherTextException;
import peer.PeerConnection;
import peer.PeerMessage;
import util.CipherUtil;
import Node.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RFILHandler implements MessageHandler{

    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        RandomAccessFile f = null;
        byte[] key = null;
        try {
            f = new RandomAccessFile("./shared_files/" + m.getIdGroup() + "/key", "r");
            key = new byte[(int)f.length()];
            f.readFully(key);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        File fileAsked = null;
        try {
            fileAsked = new File("./" + m.getIdGroup() + new String(CipherUtil.AESDecrypt(CipherUtil.erasePadding(m.getMessageContent(), PeerMessage.PADDING_START), key)));
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }
        if(fileAsked.exists() && !fileAsked.isDirectory()) {
            n.filenameUploaded = fileAsked.getName();
            try {
                n.sendFileToPeer(fileAsked, m.getIdGroup(), m.getIdFrom());
            } catch (IOException e) {
                e.printStackTrace();
            }
            n.filenameUploaded = null;
            //SEND FILE

        }
        else{
            c.sendMessage(new PeerMessage(MessageType.NFIL, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), m.getMessageContent()));
        }
    }
}
