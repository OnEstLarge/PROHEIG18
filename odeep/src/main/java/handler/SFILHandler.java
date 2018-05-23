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
            System.out.println("FAIL");
            Client.sendPM(new PeerMessage(MessageType.PGET, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), m.getNoPacket(), new byte[]{}));
            return;
        }

        //cas du premier paquet : le paquet contient uniquement le nom et la taille du fichier
        if (m.getNoPacket() == 0) {
            String[] fileInfo = new String(rcv).split(":");
            n.filesizeDownloaded.put(m.getIdFrom(), Integer.parseInt(fileInfo[1]));
            n.filenameDownloaded.put(m.getIdFrom(), fileInfo[0]);
            int size = n.filesizeDownloaded.get(m.getIdFrom()) / PeerMessage.MESSAGE_CONTENT_SIZE + 2;
            n.numberPacketDownloaded.put(m.getIdFrom(), size);
            n.numberPacketCurrent.put(m.getIdFrom(), 0);
            n.listPacket.put(m.getIdFrom(), new ArrayList<>());
            for(int i = 0; i < n.numberPacketDownloaded.get(m.getIdFrom()); i++){
                n.listPacket.get(m.getIdFrom()).add(false);
            }
            System.out.println("Receiving " + fileInfo[0]);
            try {
                RandomAccessFile emptyFile = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + m.getIdGroup() + "/" + n.filenameDownloaded.get(m.getIdFrom()), "rw");

                emptyFile.setLength(n.filesizeDownloaded.get(m.getIdFrom()));
                emptyFile.close();
                n.listPacket.get(m.getIdFrom()).set(0,true);
                n.numberPacketCurrent.put(m.getIdFrom(), n.numberPacketCurrent.get(m.getIdFrom()) + 1);
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
                RandomAccessFile raf = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + m.getIdGroup() + "/" + n.filenameDownloaded.get(m.getIdFrom()), "rw");
                raf.seek(PeerMessage.MESSAGE_CONTENT_SIZE * (m.getNoPacket() - 1));
                raf.write(rcv);
                raf.close();
                n.listPacket.get(m.getIdFrom()).set(m.getNoPacket(), true);
                n.numberPacketCurrent.put(m.getIdFrom(), n.numberPacketCurrent.get(m.getIdFrom()) + 1);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        try {
            Client.updateDownloadBar((double) n.numberPacketCurrent.get(m.getIdFrom()) / n.numberPacketDownloaded.get(m.getIdFrom()));
        }
        catch (NullPointerException e){
            System.out.println("BAR ERREUR");
        }
    }
}
