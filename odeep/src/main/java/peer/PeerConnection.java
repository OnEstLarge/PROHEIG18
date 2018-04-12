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


    public PeerConnection(Socket socket) {
        clientSocket = socket; //vérification sur socket?
    }

    public PeerConnection(PeerInformations peer) throws IOException{
        this.peer = peer;

        clientSocket = new Socket(peer.getAddress(), peer.getPort());
        os = clientSocket.getOutputStream();
        is = new BufferedInputStream(clientSocket.getInputStream());
    }

    public PeerMessage receiveMessage() throws InvalidFormatException{
        byte[] b = new byte[4096];
        try{
            while(is.read(b) != -1) {

            }
        } catch(IOException e) {

        }
        return new PeerMessage(b);
    }

    public void sendMessage(PeerMessage message) {
        try {
            os.write(message.getFormattedMessage());
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
