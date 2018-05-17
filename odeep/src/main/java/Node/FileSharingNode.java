package Node;

import User.Group;
import User.Person;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;
import util.CipherUtil;
import util.JSONUtil;

import java.io.*;

public class FileSharingNode extends Node {

    public FileSharingNode(PeerInformations myInfos) {
        super(myInfos);
    }

    /**
     * methode permettant l'envoie d'un fichier entre membre d'un meme groupe
     *
     * @param file        fichier a envoyer
     * @param groupID
     * @param destination
     * @throws IOException
     */
    public void sendFileToPeer(File file, String groupID, PeerInformations destination) throws IOException {
        byte[] key = this.getKey(groupID);
        int index = 0;
        PeerConnection c = new PeerConnection(destination);
        String filename = file.getName();
        long fileSize = file.length();
        String fileInfo = filename + ":" + Long.toString(fileSize);
        byte[] cipherFileInfo = CipherUtil.AESEncrypt(fileInfo.getBytes(), key);


        c.sendMessage(new PeerMessage(MessageType.SFIL, groupID, this.getNodePeer().getID(), destination.getID(), index, cipherFileInfo));
        c.close();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        index++;
        byte[] mes = new byte[PeerMessage.MESSAGE_CONTENT_SIZE];

        for (int i = 0; i < (fileSize / PeerMessage.MESSAGE_CONTENT_SIZE); i++, index++) {
            PeerConnection c2 = new PeerConnection(destination);
            RandomAccessFile raf = new RandomAccessFile("./shared_files/" + groupID + "/" + filename, "rw");
            raf.seek(PeerMessage.MESSAGE_CONTENT_SIZE * i);
            raf.read(mes, 0, mes.length);
            raf.close();
            byte[] cipherMes = CipherUtil.AESEncrypt(mes, key);
            PeerMessage p = new PeerMessage(MessageType.SFIL, groupID, this.getNodePeer().getID(), destination.getID(), index, cipherMes);
            c2.sendMessage(p);
            c2.close();
        }

        byte[] lastMes = new byte[(int) (fileSize % PeerMessage.MESSAGE_CONTENT_SIZE)];
        PeerConnection c2 = new PeerConnection(destination);
        RandomAccessFile raf = new RandomAccessFile("./shared_files/" + groupID + "/" + filename, "rw");
        raf.seek(PeerMessage.MESSAGE_CONTENT_SIZE * (fileSize/PeerMessage.MESSAGE_CONTENT_SIZE));
        raf.read(lastMes, 0, lastMes.length);
        raf.close();
        byte[] cipherMes = CipherUtil.AESEncrypt(lastMes, key);
        PeerMessage p = new PeerMessage(MessageType.SFIL, groupID, this.getNodePeer().getID(), destination.getID(), index, cipherMes);
        c2.sendMessage(p);
        c2.close();
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
            payload = new byte[(int)f.length()];
            f.readFully(payload);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Group g = JSONUtil.parseJson(new String(payload), Group.class);
        for (Person p : g.getMembers()){
            for(String file : p.getFiles()){
                if (file.equals(filename)){
                    for(PeerInformations pi : getKnownPeers()){
                        if(pi.getID().equals(p.getID())){
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
     * @param filename    nom du fichier
     * @param groupID     groupe dans lequel la requete est effectuée
     * @param destination pair vers lequel la requete est envoyée
     *                    remarque : le pair qui recoit cette requete cherchera le fichier uniquement dans le dossier du groupe passé en paramètre
     */
    public void requestFile(String filename, String groupID, PeerInformations destination) {
        if (filename == null || groupID == null || destination == null) {
            throw new NullPointerException();
        }
        byte[] buffer = filename.getBytes();
        PeerInformations peerHavingFile = getFileLocation(filename, groupID);
        sendToPeer(new PeerMessage(MessageType.RFIL.toString(), groupID, this.getNodePeer().getID(), peerHavingFile.getID(), buffer), destination);
    }

    /**
     * Demande a un pair d'envoyer un fichier a travers un groupe donné
     *
     * @param filename    nom du fichier
     * @param groupID     nom du groupe
     * @param destination pair destinataire de la requete
     */
    public void askForFile(String filename, String groupID, PeerInformations destination) {
        if (filename == null || groupID == null || destination == null) {
            throw new NullPointerException();
        }
        byte[] buffer = filename.getBytes();
        sendToPeer(new PeerMessage(MessageType.FGET.toString(), groupID, this.getNodePeer().getID(), destination.getID(), buffer), destination);
    }

    public void test() throws IOException {
        File[] filesList = new File(".").listFiles();
        for (File f : filesList) {
            if (f.isFile()) {
                System.out.println(f.getName());
            }
        }
        sendFileToPeer(new File("./testfile"), "0", new PeerInformations("test", "127.0.0.1", 80));

    }


}
