package handler;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.DHR1Handler.java
 Auteur(s)   : Burgbacher Lionel, Jee Mathieu, Kopp Olivier, Piller Florent,
               Silvestri Romain, Schürch Loïc
 Date        : 18.05.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import main.Client;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerMessage;
import util.CipherUtil;
import Node.Node;
import util.Constant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Classe permettant le traitement d'un message de type PGET
 */
public class PGETHandler implements MessageHandler {

    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        //Ce message est envoyé pour demander un paquet d'un fichier

        byte[] mes = new byte[PeerMessage.MESSAGE_CONTENT_SIZE];

        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + m.getIdGroup() + "/" + n.filenameUploaded.get(m.getIdFrom()), "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //premier paquet correspond aux infos du fichier
        if (m.getNoPacket() == 0) {
            String filename = n.filenameUploaded.get(m.getIdFrom());
            long fileSize = new File(Constant.ROOT_GROUPS_DIRECTORY + "/" + m.getIdGroup() + "/" + filename).length();
            String fileInfo = filename + ":" + Long.toString(fileSize);
            byte[] cipherFileInfo = CipherUtil.AESEncrypt(fileInfo.getBytes(), n.getKey(m.getIdGroup()));
            Client.sendPM(new PeerMessage(MessageType.SFIL, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), 0, cipherFileInfo));
        } else {
            try {
                //on relis le contenu du paquet dans le fichier
                raf.seek(PeerMessage.MESSAGE_CONTENT_SIZE * (m.getNoPacket() - 1));
                raf.read(mes, 0, PeerMessage.MESSAGE_CONTENT_SIZE);
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //on envoie le paquet
            byte[] cipherMes = CipherUtil.AESEncrypt(mes, n.getKey(m.getIdGroup()));
            PeerMessage response = new PeerMessage(MessageType.SFIL, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), m.getNoPacket(), cipherMes);
            Client.sendPM(response);
        }
    }
}
