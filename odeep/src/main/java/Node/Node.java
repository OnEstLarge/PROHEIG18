package Node;
/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : Node.Node.java
 Auteur(s)   : Burgbacher Lionel, Jee Mathieu, Schürch Loïc
 Date        : 15.03.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/


import User.Group;
import User.Person;
import handler.RSAInfo;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerHandler;
import peer.PeerInformations;
import peer.PeerMessage;
import util.CipherUtil;
import util.JSONUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Noeud P2P.
 */
public class Node {

    public Node(PeerInformations myInfos) {
        this.myInfos = myInfos;

        knownPeers = new ArrayList<PeerInformations>();
        mapMessage = new HashMap<String, MessageHandler>();
    }

    /**
     * Ajoute un ou plusieurs pairs à la liste de pairs connus.
     *
     * @param peers nouveau(x) pair(s) à ajouter
     */
    public void addPeer(PeerInformations... peers) {
        for (PeerInformations peer : peers) {
            if (!knownPeers.contains(peer)) {
                knownPeers.add(peer);
            }
        }
    }

    /**
     * Ajoute un nouveau type de message accompagné de son handler dans la map 'mapMessage'.
     *
     * @param typeMessage type de message reçu/envoyé (format: 4 lettres majuscules, ex: XXXX)
     * @param handler     handler correspondant au type de message
     */
    public void addMessageHandler(String typeMessage, MessageHandler handler) throws IllegalArgumentException {
        if (!PeerMessage.isValidTypeFormat(typeMessage)) {
            throw new IllegalArgumentException("Bad type format");
        }
        mapMessage.put(typeMessage, handler);
    }

    /**
     * Ferme la connexion entrante du Noeud (Node.Node).
     */
    public void shutdown() {
        nodeIsRunning = false;
    }

    /**
     * Ouvre la connexion entrante du Noeud (Node.Node).
     */
    public void turnOn() {
        nodeIsRunning = true;
    }

    public ArrayList<PeerInformations> getKnownPeers() {
        return new ArrayList<PeerInformations>(knownPeers);
    }

    /**
     * Retire un pair de la map 'mapMessage'.
     *
     * @param peers pair(s) à retirer
     */
    public void removeKnownPeers(PeerInformations... peers) {
        for (PeerInformations peer : peers) {
            knownPeers.remove(peer);
        }
    }

    public PeerInformations getNodePeer() {
        return myInfos;
    }

    public HashMap<String, MessageHandler> getMapMessage() {
        return new HashMap<String, MessageHandler>(mapMessage);
    }

    /**
     *
     */
    public void acceptingConnections() {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(myInfos.getPort());
            while (nodeIsRunning) {
                //socket wait for connection
                try {
                    clientSocket = serverSocket.accept();

                    PeerHandler peerHandler = new PeerHandler(this, clientSocket);
                } catch (IOException ex) {
                    //TODO
                }
            }

        } catch (IOException ex) {

            return; //A gerer
        } finally {
            // close clientSocket
            cleanup(null, null, clientSocket, serverSocket);
        }
    }

    /**
     * methode permettant l'envoie d'un fichier entre membre d'un meme groupe
     *
     * @param file        fichier a envoyer
     * @param groupID
     * @param destination
     * @throws IOException
     */
    public void sendFileToPeer(File file, String groupID, String destination) throws IOException {
        byte[] key = this.getKey(groupID);
        int index = 0;

        PeerInformations pi = null;
        for (PeerInformations p : this.getKnownPeers()) {
            if (p.getID().equals(destination)) {
                pi = p;
                break;
            }
        }
        if (pi == null) {
            throw new NullPointerException();
        } else {
            String filename = file.getName();
            long fileSize = file.length();
            String fileInfo = filename + ":" + Long.toString(fileSize);
            byte[] cipherFileInfo = CipherUtil.AESEncrypt(fileInfo.getBytes(), key);

            this.createTempConnection(pi, new PeerMessage(MessageType.SFIL, groupID, this.getNodePeer().getID(), destination, index, cipherFileInfo));

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            index++;
            byte[] mes = new byte[PeerMessage.MESSAGE_CONTENT_SIZE];

            for (int i = 0; i < (fileSize / PeerMessage.MESSAGE_CONTENT_SIZE); i++, index++) {
                RandomAccessFile raf = new RandomAccessFile("./shared_files/" + groupID + "/" + filename, "rw");
                raf.seek(PeerMessage.MESSAGE_CONTENT_SIZE * i);
                raf.read(mes, 0, mes.length);
                byte[] newMes = CipherUtil.eraseZero(mes);
                raf.close();
                byte[] cipherMes = CipherUtil.AESEncrypt(newMes, key);
                PeerMessage p = new PeerMessage(MessageType.SFIL, groupID, this.getNodePeer().getID(), destination, index, cipherMes);
                System.out.println("sending : " + filename + " : " + 100.0 * i / (fileSize / PeerMessage.MESSAGE_CONTENT_SIZE) + "%");
                this.createTempConnection(pi, p);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            byte[] lastMes = new byte[(int) (fileSize % PeerMessage.MESSAGE_CONTENT_SIZE)];
            RandomAccessFile raf = new RandomAccessFile("./shared_files/" + groupID + "/" + filename, "rw");
            raf.seek(PeerMessage.MESSAGE_CONTENT_SIZE * (fileSize / PeerMessage.MESSAGE_CONTENT_SIZE));
            raf.read(lastMes, 0, lastMes.length);
            raf.close();
            byte[] cipherMes = CipherUtil.AESEncrypt(lastMes, key);
            PeerMessage p = new PeerMessage(MessageType.SFIL, groupID, this.getNodePeer().getID(), destination, index, cipherMes);
            this.createTempConnection(pi, p);
        }
    }

    /**
     * cette fonction retourne la liste des personne presentes dans un groupe possedant un certain fichier
     *
     * @param filename fichier recherché
     * @param groupID  nom du groupe
     * @return liste de peerInformation
     */
    public PeerInformations getFileLocation(String filename, String groupID) {
        RandomAccessFile f = null;
        byte[] payload = null;
        try {
            f = new RandomAccessFile("./shared_files/" + groupID + "/config.json", "r");
            payload = new byte[(int) f.length()];
            f.readFully(payload);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Group g = JSONUtil.parseJson(new String(payload), Group.class);
        for (Person p : g.getMembers()) {
            for (String file : p.getFiles()) {
                if (file.equals(filename)) {
                    for (PeerInformations pi : getKnownPeers()) {
                        if (pi.getID().equals(p.getID())) {
                            return pi;
                        }
                    }
                    return null;
                }
            }
        }
        return null;
    }


    /**
     * cette fonction envoie une requete afin de determiner si un pair possède effectivement un fichier
     *
     * @param filename nom du fichier
     * @param groupID  groupe dans lequel la requete est effectuée
     *                 remarque : le pair qui recoit cette requete cherchera le fichier uniquement dans le dossier du groupe passé en paramètre
     */
    public void requestFile(String filename, String groupID) {
        if (filename == null || groupID == null) {
            throw new NullPointerException();
        }
        byte[] buffer = filename.getBytes();
        PeerInformations peerHavingFile = getFileLocation(filename, groupID);

        PeerConnection p = null;
        try {
            p = new PeerConnection(peerHavingFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.sendMessage(new PeerMessage(MessageType.RFIL, groupID, this.getNodePeer().getID(), peerHavingFile.getID(), CipherUtil.AESEncrypt(buffer, this.getKey(groupID))));
        p.close();
    }

    /**
     *
     */
    private void cleanup(OutputStream out, InputStream in, Socket clientSocket, ServerSocket serverSocket) {
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public RSAInfo getTempRSAInfo() {
        return tempRsaInfo;
    }

    public void setTempRSAInfo(RSAInfo tempRSAInfo) {
        this.tempRsaInfo = tempRSAInfo;
    }

    public void setKey(byte[] key, String group) {
        File keyFile = new File("./shared_files/" + group + "/key");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(keyFile);
            fos.write(key);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getKey(String group) {
        RandomAccessFile f = null;
        byte[] key = null;
        try {
            f = new RandomAccessFile("./shared_files/" + group + "/key", "r");
            key = new byte[(int) f.length()];
            f.readFully(key);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return key;
    }

    public static void createTempConnection(PeerInformations peer, PeerMessage message) {
        PeerConnection p = null;
        try {
            p = new PeerConnection(peer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.sendMessage(message);
        p.close();
    }

    // Informations sur le pair de ce noeud
    private PeerInformations myInfos;

    // Les informations sur les pairs que ce noeud connait
    private ArrayList<PeerInformations> knownPeers;

    // Association entre les types de message et leur handlers
    private HashMap<String, MessageHandler> mapMessage;

    // Permet de déterminer sur le noeud est actif
    private boolean nodeIsRunning = true;

    //permet de conserver temporairement la pair de clé RSA utilisé lors d'un protocole Diffie Hellman
    private RSAInfo tempRsaInfo = null;

    public static String filenameUploaded = null;
    public static int filesizeUploaded = 0;

    public static String filenameDownloaded = null;
    public static int filesizeDownloaded = 0;

}
