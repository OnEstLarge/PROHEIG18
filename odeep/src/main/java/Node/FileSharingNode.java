package Node;

import message.MessageType;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;

import java.io.*;
import java.net.Socket;

public class FileSharingNode extends Node {

    public FileSharingNode(PeerInformations myInfos) {
        super(myInfos);
        /*try {
            test();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * methode permettant l'envoie d'un fichier entre membre d'un meme groupe
     * @param file fichier a envoyer
     * @param groupID
     * @param destination
     * @throws IOException
     */
    public void sendFileToPeer(File file, String groupID, PeerInformations destination) throws IOException {
        if (file == null || groupID == null || destination == null){
            throw new NullPointerException();
        }
        byte chunk[] = new byte[4032];
        FileInputStream fis = new FileInputStream(file);

        //on crée la connexion
        PeerConnection c = new PeerConnection(destination);

        //on envoie la taille du fichier à envoyer
        c.sendMessage(new PeerMessage(MessageType.SFIL, groupID, this.getNodePeer().getID(), destination.getID(),(""+file.length()).getBytes()));
            int chunkLen = 0;
            int remaining = (int)file.length();
            int i = 0;
            while ((chunkLen = fis.read(chunk, 0, Math.min(4032, remaining))) != -1){
                remaining -= chunkLen;
                //on envoie les morceaux du fichier
                c.sendMessage(new PeerMessage(MessageType.SFIL, groupID, this.getNodePeer().getID(), destination.getID(), i, chunk));
                i++;
                System.out.println(((i+1)*chunk.length)/(double)file.length()*100 % 1 + "%");
                chunk = new byte[remaining];
            }
        fis.close();
    }

    /**
     * cette fonction retourne la liste des personne presentes dans un groupe possedant un certain fichier
     * @param filename fichier recherché
     * @param groupID nom du groupe
     * @return liste de peerInformation
     */
    public PeerInformations[] getFileLocation(String filename, String groupID){return null;}


    /**
     * cette fonction envoie une requete afin de determiner si un pair possède effectivement un fichier
     * @param filename nom du fichier
     * @param groupID groupe dans lequel la requete est effectuée
     * @param destination pair vers lequel la requete est envoyée
     * remarque : le pair qui recoit cette requete cherchera le fichier uniquement dans le dossier du groupe passé en paramètre
     */
    public void requestFile(String filename, String groupID, PeerInformations destination){
        if (filename == null || groupID == null || destination == null){
            throw new NullPointerException();
        }
        byte buffer[] = new byte [4032];
        buffer = filename.getBytes();
        PeerInformations[] peerHavingFile = getFileLocation(filename, groupID);
        for(PeerInformations peer : peerHavingFile) {
            sendToPeer(new PeerMessage(MessageType.RFIL.toString(), groupID, this.getNodePeer().getID(), peer.getID(), buffer), destination);
        }
    }

    /**
     * Demande a un pair d'envoyer un fichier a travers un groupe donné
     * @param filename nom du fichier
     * @param groupID nom du groupe
     * @param destination pair destinataire de la requete
     */
    public void askForFile(String filename, String groupID, PeerInformations destination){
        if (filename == null || groupID == null || destination == null){
            throw new NullPointerException();
        }
        byte buffer[] = new byte [4032];
        buffer = filename.getBytes();
        sendToPeer(new PeerMessage(MessageType.FGET.toString(), groupID, this.getNodePeer().getID(), destination.getID(), buffer), destination);
    }

    public void test() throws IOException {
        File[] filesList = new File(".").listFiles();
        for(File f : filesList){
            if(f.isFile()){
                System.out.println(f.getName());
            }
        }
        sendFileToPeer(new File("./testfile"),"0", new PeerInformations("test","127.0.0.1", 80));

    }


}
