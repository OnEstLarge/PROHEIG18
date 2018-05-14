package handler;

import Node.Node;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;
import util.CipherUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PGETHandler implements MessageHandler{

    @Override
    public void handleMessage(PeerConnection c, PeerMessage m) {
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
        byte[] mes = new byte[PeerMessage.MESSAGE_CONTENT_SIZE];

        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile("./shared_files/" + m.getIdGroup() + "/" + Node.filename, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            raf.seek(PeerMessage.MESSAGE_CONTENT_SIZE * (m.getNoPacket()-1));
            raf.read(mes, 0, PeerMessage.MESSAGE_CONTENT_SIZE);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
            byte[] cipherMes = CipherUtil.AESEncrypt(mes, key);
            PeerMessage p = new PeerMessage(MessageType.SFIL, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), m.getNoPacket(), cipherMes);
            c.sendMessage(p);
            c.close();

    }
}
