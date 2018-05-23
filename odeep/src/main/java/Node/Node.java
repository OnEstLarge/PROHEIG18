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
import main.Client;
import message.MessageHandler;
import message.MessageType;
import org.bouncycastle.crypto.InvalidCipherTextException;
import peer.PeerHandler;
import peer.PeerInformations;
import peer.PeerMessage;
import util.CipherUtil;
import util.Constant;
import util.InterfaceUtil;
import util.JSONUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

/**
 * Noeud P2P.
 */
public class Node {

    public Node(PeerInformations myInfos) {
        this.myInfos = myInfos;

        mapMessage = new HashMap<String, MessageHandler>();
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

    public PeerInformations getNodePeer() {
        return myInfos;
    }

    public HashMap<String, MessageHandler> getMapMessage() {
        return new HashMap<>(mapMessage);
    }

    /**
     * permet d'accepter des connections entrantes sur le noeud
     */
    public void acceptingConnections() {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(myInfos.getPort());
            while (nodeIsRunning) {
                //attente de connection
                try {
                    clientSocket = serverSocket.accept();

                    PeerHandler peerHandler = new PeerHandler(this, clientSocket);
                } catch (IOException ex) {
                }
            }

        } catch (IOException ex) {
            return;
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
        //récupération des infos du fichier
        String filename = file.getName();
        long fileSize = file.length();
        String fileInfo = filename + ":" + Long.toString(fileSize);
        byte[] cipherFileInfo = CipherUtil.AESEncrypt(fileInfo.getBytes(), key);

        Client.clearUploadBar();
        //envoie des infos du fichier
        Client.sendPM(new PeerMessage(MessageType.SFIL, groupID, this.getNodePeer().getID(), destination, index, cipherFileInfo));

        //attente afin de laisser le temps au destinataire d'allouer l'espace pour le fichier
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        index++;
        byte[] mes = new byte[PeerMessage.MESSAGE_CONTENT_SIZE];

        //envoie de tout les paquets contenue dans le fichier dans l'ordre
        for (int i = 0; i < (fileSize / PeerMessage.MESSAGE_CONTENT_SIZE); i++, index++) {
            RandomAccessFile raf = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + groupID + "/" + filename, "rw");
            raf.seek(PeerMessage.MESSAGE_CONTENT_SIZE * i);
            raf.read(mes, 0, mes.length);
            raf.close();

            byte[] newMes = CipherUtil.eraseZero(mes);
            byte[] cipherMes = CipherUtil.AESEncrypt(newMes, key);
            PeerMessage p = new PeerMessage(MessageType.SFIL, groupID, this.getNodePeer().getID(), destination, index, cipherMes);

            Client.updateUploadBar(((double) i) / (fileSize / PeerMessage.MESSAGE_CONTENT_SIZE));
            Client.sendPM(p);
            //limitation du débit suite au problèmes sur les bufferedStream
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //envoie du dernier paquet du fichier
        //traiter à part car sa taille est différente
        byte[] lastMes = new byte[(int) (fileSize % PeerMessage.MESSAGE_CONTENT_SIZE)];
        RandomAccessFile raf = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + groupID + "/" + filename, "rw");
        raf.seek(PeerMessage.MESSAGE_CONTENT_SIZE * (fileSize / PeerMessage.MESSAGE_CONTENT_SIZE));
        raf.read(lastMes, 0, lastMes.length);
        raf.close();
        byte[] cipherMes = CipherUtil.AESEncrypt(lastMes, key);
        PeerMessage p = new PeerMessage(MessageType.SFIL, groupID, this.getNodePeer().getID(), destination, index, cipherMes);
        Client.sendPM(p);

        //envoie du paquet de fin d'envoie
        PeerMessage pm = new PeerMessage(MessageType.SFIL, groupID, this.getNodePeer().getID(), destination, 99999999, cipherMes);
        Client.sendPM(pm);
        Client.updateUploadBar(1.0);
    }

    /**
     * cette fonction retourne la liste des personne presentes dans un groupe possedant un certain fichier
     *
     * @param filename fichier recherché
     * @param groupID  nom du groupe
     * @return liste de Username
     */
    public String getFileLocation(String filename, String groupID) {
        RandomAccessFile f = null;
        byte[] payload = null;
        byte[] plainPayload = null;
        try {
            //récupération du fichier de config
            f = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + groupID + "/" + Constant.CONFIG_FILENAME, "r");
            payload = new byte[(int) f.length()];
            f.readFully(payload);
            plainPayload = CipherUtil.AESDecrypt(payload, this.getKey(groupID));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }
        //parcours du groupe
        Group g = JSONUtil.parseJson(new String(plainPayload), Group.class);
        for (Person p : g.getMembers()) {
            for (String file : p.getFiles()) {
                if (file.equals(filename)) {
                    return p.getID();
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
        //selectionne une personne qui possède normalement un fichier
        String peerHavingFile = getFileLocation(filename, groupID);
        if (peerHavingFile == null) {
            return;
        }

        //envoie de la requete
        Client.sendPM(new PeerMessage(MessageType.RFIL, groupID, this.getNodePeer().getID(), peerHavingFile, CipherUtil.AESEncrypt(buffer, this.getKey(groupID))));
    }

    /**
     * Cette fonction assure que tout les paquets d'un fichier téléchargé sont bien arrivé
     * @param pm dernier message recu par la personne ayant envoyé le fichier
     */
    public void checkPacket(PeerMessage pm) {
        Boolean allPacketOk = true;
        //on envoie un PGET pour tous les paquet encore à false
        for (int i = 0; i < listPacket.get(pm.getIdFrom()).size(); i++) {
            boolean b = listPacket.get(pm.getIdFrom()).get(i);
            if (!b) {
                Client.sendPM(new PeerMessage(MessageType.PGET, pm.getIdGroup(), pm.getIdTo(), pm.getIdFrom(), i, new byte[]{}));
            }
        }
        if (!allPacketOk) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            checkPacket(pm);
        } else {
            //fin du téléchargement
            Client.updateDownloadBar(1.0);
            InterfaceUtil.addFile(new File(Constant.ROOT_GROUPS_DIRECTORY + "/" + pm.getIdGroup() + "/" + filenameDownloaded.get(pm.getIdFrom())), Client.getUsername(), Client.getGroupById(pm.getIdGroup()));
            //reset des valeurs dans la hashmap pour permettre un prochaine telechargement
            filesizeDownloaded.put(pm.getIdFrom(), null);
            filenameDownloaded.put(pm.getIdFrom(), null);
            numberPacketDownloaded.put(pm.getIdFrom(), null);
            numberPacketCurrent.put(pm.getIdFrom(), null);
            listPacket.put(pm.getIdFrom(), null);
        }
    }

    /**
     * permet de fermer le noeuds
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

    /**
     * Permet de stocker la clé symétrique dans un fichier en local
     * @param key clé à stocker
     * @param group groupe lié à la clé
     */
    public void setKey(byte[] key, String group) {
        File keyFile = new File(Constant.ROOT_GROUPS_DIRECTORY + "/" + group + "/" + Constant.KEY_FILENAME);
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

    /**
     * récupération de la clé symétrique depuis le fichier local
     * @param group groupe lié à la clé
     * @return la clé dans un tableau de bytes
     */
    public byte[] getKey(String group) {
        RandomAccessFile f = null;
        byte[] key = null;
        try {
            f = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + group + "/" + Constant.KEY_FILENAME, "r");
            key = new byte[(int) f.length()];
            f.readFully(key);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return key;
    }

    // Informations sur le pair de ce noeud
    private PeerInformations myInfos;

    // Association entre les types de message et leur handlers
    private HashMap<String, MessageHandler> mapMessage;

    // Permet de déterminer sur le noeud est actif
    private boolean nodeIsRunning = true;

    //permet de conserver temporairement la pair de clé RSA utilisé lors d'un protocole Diffie Hellman
    private RSAInfo tempRsaInfo = null;

    //hashmap permettant l'upload de plusieurs fichiers simultanémant
    public static HashMap<String, String> filenameUploaded = new HashMap<>();

    //hashmap permettant le download de plusieurs fichiers simultanémant
    public static HashMap<String, String> filenameDownloaded = new HashMap<>();
    public static HashMap<String, Integer> filesizeDownloaded = new HashMap<>();
    public static HashMap<String, Integer> numberPacketDownloaded = new HashMap<>();
    public static HashMap<String, Integer> numberPacketCurrent = new HashMap<>();
    public static HashMap<String, List<Boolean>> listPacket = new HashMap<>();
}
