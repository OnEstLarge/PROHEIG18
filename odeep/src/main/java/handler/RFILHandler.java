package handler;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.RFILHandler.java
 Auteur(s)   : Kopp Olivier
 Date        : 10.05.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import main.Client;
import message.MessageHandler;
import message.MessageType;
import org.bouncycastle.crypto.InvalidCipherTextException;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;
import util.CipherUtil;
import Node.Node;
import util.Constant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Classe permettant le traitement d'un message de type RFIL
 */
public class RFILHandler implements MessageHandler{

    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        RandomAccessFile f = null;
        byte[] key = null;
        try {
            f = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + m.getIdGroup() + "/" + Constant.KEY_FILENAME, "r");
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
            fileAsked = new File(Constant.ROOT_GROUPS_DIRECTORY + "/" + m.getIdGroup() + "/" + new String(CipherUtil.AESDecrypt(m.getMessageContent(), key)));
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }

        if(fileAsked.exists() && !fileAsked.isDirectory()) {
            //on ne peut telecharger qu'un seul fichier chez un noeud particulier
            if(n.filenameUploaded.get(m.getIdFrom()) == null) {
                n.filenameUploaded.put(m.getIdFrom(), fileAsked.getName());
                try {
                    n.sendFileToPeer(fileAsked, m.getIdGroup(), m.getIdFrom());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                n.filenameUploaded.put(m.getIdFrom(), null);
            }

        }
        else{
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
                n.createTempConnection(pi,new PeerMessage(MessageType.NFIL, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), m.getMessageContent()) );
            }
            */
            Client.sendPM(new PeerMessage(MessageType.NFIL, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), m.getMessageContent()));
        }
    }
}
