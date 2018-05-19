package handler;

import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;
import util.CipherUtil;
import Node.Node;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PGETHandler implements MessageHandler {

    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        c.close();
        RandomAccessFile f = null;
        byte[] key = null;
        try {
            f = new RandomAccessFile("./shared_files/" + m.getIdGroup() + "/key", "r");
            key = new byte[(int) f.length()];
            f.readFully(key);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] mes = new byte[PeerMessage.MESSAGE_CONTENT_SIZE];

        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile("./shared_files/" + m.getIdGroup() + "/" + n.filenameUploaded, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            raf.seek(PeerMessage.MESSAGE_CONTENT_SIZE * (m.getNoPacket() - 1));
            raf.read(mes, 0, PeerMessage.MESSAGE_CONTENT_SIZE);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] cipherMes = CipherUtil.AESEncrypt(mes, key);
        PeerMessage response = new PeerMessage(MessageType.SFIL, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), m.getNoPacket(), cipherMes);
        PeerInformations pi = null;
        for (PeerInformations p : n.getKnownPeers()) {
            if (p.getID().equals(m.getIdFrom())) {
                pi = p;
                break;
            }
        }
        if (pi == null) {
            throw new NullPointerException();
        } else {
            n.createTempConection(pi, response);
        }
    }
}
