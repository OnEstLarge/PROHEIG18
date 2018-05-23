package handler;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.PGETHandler.java
 Auteur(s)   : Kopp Olivier
 Date        : 18.05.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import com.sun.org.apache.bcel.internal.generic.InstructionConstants;
import main.Client;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;
import util.CipherUtil;
import Node.Node;
import util.Constant;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Classe permettant le traitement d'un message de type PGET
 */
public class PGETHandler implements MessageHandler {

    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {


        System.out.println("\nPGET  PGET PPGET  PGET\n");

        RandomAccessFile f = null;
        byte[] key = null;
        try {
            f = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + m.getIdGroup() + "/" + Constant.KEY_FILENAME, "r");
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
            raf = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + m.getIdGroup() + "/" + n.filenameUploaded.get(m.getIdFrom()), "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            raf.seek(PeerMessage.MESSAGE_CONTENT_SIZE * (m.getNoPacket() == 0 ? m.getNoPacket() : m.getNoPacket()-1));
            raf.read(mes, 0, PeerMessage.MESSAGE_CONTENT_SIZE);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] cipherMes = CipherUtil.AESEncrypt(mes, key);
        PeerMessage response = new PeerMessage(MessageType.SFIL, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), m.getNoPacket(), cipherMes);
        /*
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
            n.createTempConnection(pi, response);
        }
        */
        Client.sendPM(response);
    }
}
