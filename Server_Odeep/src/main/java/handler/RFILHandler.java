package handler;

import message.MessageHandler;
import message.MessageType;
import org.bouncycastle.crypto.InvalidCipherTextException;
import peer.PeerConnection;
import peer.PeerMessage;
import util.CipherUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RFILHandler implements MessageHandler{

    @Override
    public void handleMessage(PeerConnection c, PeerMessage m) {
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile("./" + m.getIdGroup() + "/key", "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] key = new byte[0];
        try {
            key = new byte[(int)f.length()];
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            f.readFully(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File fileAsked = null;
        try {
            fileAsked = new File("./" + m.getIdGroup() + new String(CipherUtil.AESDEcrypt(CipherUtil.erasePadding(m.getMessageContent(), PeerMessage.PADDING_START), key)));
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }
        if(fileAsked.exists() && !fileAsked.isDirectory()) {
            //SEND FILE
        }
        else{
            c.sendMessage(new PeerMessage(MessageType.NFIL, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), m.getMessageContent()));
        }
    }
}
