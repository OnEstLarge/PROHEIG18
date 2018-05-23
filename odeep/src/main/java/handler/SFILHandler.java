package handler;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.SFILHandler.java
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

import java.io.*;
import java.util.ArrayList;

/**
 * Classe permettant le traitement d'un message de type SFIL
 */
public class SFILHandler implements MessageHandler {

    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
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

        byte[] rcv = new byte[0];
        try {
            rcv = CipherUtil.AESDecrypt(m.getMessageContent(), key);
        } catch (InvalidCipherTextException e) {
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
                n.createTempConnection(pi, new PeerMessage(MessageType.PGET, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), m.getNoPacket(), new byte[]{}));
                return;
            }
            */
            System.out.println("FAIL");
            Client.sendPM(new PeerMessage(MessageType.PGET, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), m.getNoPacket(), new byte[]{}));
            return;
        }

        //cas du premier paquet : le paquet contient uniquement le nom et la taille du fichier
        if (m.getNoPacket() == 0) {
            String[] fileInfo = new String(rcv).split(":");
            n.filesizeDownloaded = Integer.parseInt(fileInfo[1]);
            n.filenameDownloaded = fileInfo[0];
            n.numberPacketDownloaded = n.filesizeDownloaded / PeerMessage.MESSAGE_CONTENT_SIZE + 2;
            n.numberPacketCurrent = 0;
            n.listPacket.clear();
            for(int i = 0; i < n.numberPacketDownloaded; i++){
                n.listPacket.add(false);
            }
            System.out.println("Receiving " + fileInfo[0]);
            try {
                RandomAccessFile emptyFile = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + m.getIdGroup() + "/" + n.filenameDownloaded, "rw");

                emptyFile.setLength(n.filesizeDownloaded);
                emptyFile.close();
                n.listPacket.set(0,true);
                n.numberPacketCurrent++;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(m.getNoPacket() == 99999999){
            n.checkPacket(m);
        }
        //on stocke les autres paquets dans le fichier de sortie
        else {
            try {
                RandomAccessFile raf = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + m.getIdGroup() + "/" + n.filenameDownloaded, "rw");
                raf.seek(PeerMessage.MESSAGE_CONTENT_SIZE * (m.getNoPacket() - 1));
                raf.write(rcv);
                raf.close();
                n.listPacket.set(m.getNoPacket(), true);
                n.numberPacketCurrent++;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        try {
            Client.updateDownloadBar((double) n.numberPacketCurrent / n.numberPacketDownloaded);
        }
        catch (NullPointerException e){
            System.out.println("BAR ERREUR");
        }
    }
}
