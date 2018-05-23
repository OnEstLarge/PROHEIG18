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

import User.Group;
import main.Client;
import message.MessageHandler;
import message.MessageType;
import org.bouncycastle.crypto.InvalidCipherTextException;
import peer.PeerConnection;
import peer.PeerMessage;
import util.CipherUtil;
import Node.Node;
import util.Constant;
import util.InterfaceUtil;
import util.JSONUtil;

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
        //Ce message est recu lorsque quelqu'un veut telecharger un fichier que l'on possède
        File fileAsked = null;
        try {
            fileAsked = new File(Constant.ROOT_GROUPS_DIRECTORY + "/" + m.getIdGroup() + "/" + new String(CipherUtil.AESDecrypt(m.getMessageContent(), n.getKey(m.getIdGroup()))));
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }
        //Si le fichier existe
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
        //sinon, on previent l'utilisateur que l'on ne possède pas le fichier
        else{

            Group g = Client.getGroupById(m.getIdGroup());
            InterfaceUtil.removeFile(fileAsked.getName(), m.getIdTo(), g);
            Client.sendPM(new PeerMessage(MessageType.NFIL, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), m.getMessageContent()));
        }
    }
}
