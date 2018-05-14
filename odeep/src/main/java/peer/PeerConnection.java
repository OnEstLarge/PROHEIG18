package peer;

import com.sun.media.sound.InvalidFormatException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerConnection {

    private Socket clientSocket;
    private InputStream is;
    private OutputStream os;
    private PeerInformations peer;

    public InputStream getIs() {return is;}
    public OutputStream getOs() {return os;}

    public PeerConnection(Socket socket) {

        clientSocket = socket; //vérification sur socket?
        try {
            os = clientSocket.getOutputStream();
            is = clientSocket.getInputStream();
        }catch(IOException e) { System.out.println("problem peerconnection");}
    }

    public PeerConnection(PeerInformations peer) throws IOException{
        this.peer = peer;

        clientSocket = new Socket(peer.getAddress(), peer.getPort());
        os = clientSocket.getOutputStream();
        is = clientSocket.getInputStream();
    }

    public PeerMessage receiveMessage() throws InvalidFormatException{
        byte[] b = new byte[4096];
        try{
            //on va juste lire le premier message reçu, si d'autre message arrive, c'est le handler qui gère.
            is.read(b);

        } catch(IOException e) {

        }
        System.out.println("rcv tab: " + new String(b));
        PeerMessage rcv = new PeerMessage(b);
        return rcv;
    }

    public void sendMessage(PeerMessage message) {
        try {
            System.out.println("TOSEND" + message.getType() + " --- " +new String(message.getMessageContent()));
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
