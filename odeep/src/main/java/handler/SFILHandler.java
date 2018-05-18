package handler;

import message.MessageHandler;
import message.MessageType;
import org.bouncycastle.crypto.InvalidCipherTextException;
import peer.PeerConnection;
import peer.PeerMessage;
import util.CipherUtil;
import Node.Node;

import java.io.*;

public class SFILHandler implements MessageHandler {

    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
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

        byte[] rcv = new byte[0];
        try {
            rcv = CipherUtil.AESDecrypt(CipherUtil.erasePadding(m.getMessageContent(), PeerMessage.PADDING_START), key);
        } catch (InvalidCipherTextException e) {
            c.sendMessage(new PeerMessage(MessageType.PGET, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), m.getNoPacket(), new byte[]{}));
            return;
        }
        if (m.getNoPacket() == 0) {
            String[] fileInfo = new String(rcv).split(":");
            n.filesizeDownloaded = Integer.parseInt(fileInfo[1]);
            n.filenameDownloaded = fileInfo[0];
            System.out.println("Receiving " + fileInfo[0]);
            try {
                RandomAccessFile emptyFile = new RandomAccessFile("./shared_files/" + m.getIdGroup() + "/" + n.filenameDownloaded, "rw");
                emptyFile.setLength(n.filesizeDownloaded);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                RandomAccessFile raf = new RandomAccessFile("./shared_files/" + m.getIdGroup() + "/" + n.filenameDownloaded, "rw");
                raf.seek(PeerMessage.MESSAGE_CONTENT_SIZE * (m.getNoPacket() - 1));
                raf.write(rcv);
                raf.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
