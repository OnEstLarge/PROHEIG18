package peer;

import com.sun.media.sound.InvalidFormatException;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerConnection {

    private Socket clientSocket;
    private BufferedInputStream is;
    private BufferedOutputStream os;
    private PeerInformations peer;

    public InputStream getIs() {return is;}
    public OutputStream getOs() {return os;}

    public PeerConnection(Socket socket) {

        clientSocket = socket; //vérification sur socket?
        try {
            os = new BufferedOutputStream(clientSocket.getOutputStream());
            is = new BufferedInputStream(clientSocket.getInputStream());
        }catch(IOException e) { System.out.println("problem peerconnection");}
    }

    public PeerConnection(PeerInformations peer) throws IOException{
        this.peer = peer;

        clientSocket = new Socket(peer.getAddress(), peer.getPort());
        os = new BufferedOutputStream(clientSocket.getOutputStream());
        is = new BufferedInputStream(clientSocket.getInputStream());
    }

    public PeerMessage receiveMessage() throws InvalidFormatException{
        byte[] b = new byte[PeerMessage.BLOCK_SIZE];
        try{
            //on va juste lire le premier message reçu, si d'autre message arrive, c'est le handler qui gère.
            is.read(b);

        } catch(IOException e) {

        }
        PeerMessage rcv = new PeerMessage(b);
        return rcv;
    }

    public void sendMessage(PeerMessage message) {
        try {
            os.write(message.getFormattedMessage());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
