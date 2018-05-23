package peer;

import com.sun.media.sound.InvalidFormatException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerConnection {

    private Socket clientSocket;
    private BufferedInputStream is;
    private BufferedOutputStream os;
    private PeerInformations peer;
    private boolean isLocal;

    public InputStream getIs() {return is;}
    public OutputStream getOs() {return os;}

    public PeerConnection(Socket socket, boolean isLocal) {

        this.isLocal = isLocal;
        clientSocket = socket; //vérification sur socket?
        try {
            os = new BufferedOutputStream(clientSocket.getOutputStream());
            is = new BufferedInputStream(clientSocket.getInputStream());
        }catch(IOException e) { System.out.println("problem peerconnection");}
    }

    public PeerConnection(PeerInformations peer, boolean isLocal) throws IOException{
        this.peer = peer;
        this.isLocal = isLocal;

        if(isLocal){
            clientSocket = new Socket();
            System.out.println("Adresse " + peer.getAddress());
            System.out.println("Port " + peer.getPort());
            clientSocket.connect(new InetSocketAddress(peer.getAddress(), peer.getPort()), 1000);
        }
        else {
            clientSocket = new Socket(peer.getAddress(), peer.getPort());
        }
        os = new BufferedOutputStream(clientSocket.getOutputStream());
        is = new BufferedInputStream(clientSocket.getInputStream());
    }

    public PeerMessage receiveMessage() throws InvalidFormatException{
        byte[] b = new byte[PeerMessage.BLOCK_SIZE];
        try{
            //on va juste lire le premier message reçu, si d'autre message arrive, c'est le handler qui gère.
            int read = 0;
            while (read != PeerMessage.BLOCK_SIZE) {
                read += is.read(b, read, b.length-read);
            }

        } catch(IOException e) {

        }
        System.out.println("rcv tab: " + new String(b));
        PeerMessage rcv = new PeerMessage(b);
        return rcv;
    }

    public void sendMessage(PeerMessage message) {
        try {
            //System.out.println("TOSEND" + message.getType() + " --- " +new String(message.getMessageContent()));
            //System.out.println("TOSEND" + message.getType() + " --- " + new String(message.getFormattedMessage()));
            os.write(message.getFormattedMessage());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isLocal(){
        return isLocal;
    }

    public PeerInformations getPeer() {
        return peer;
    }

    public void close() {
        try {
            is.close();
            os.close();
            clientSocket.close();
        } catch(IOException e){

        }
    }
}
