package peer;

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
        //os = clientSocket.getOutputStream();

    }

    public PeerMessage receiveMessage() {

        return null;
    }

    public void sendMessage(PeerMessage message) {
        try {
            os.write(message.getFormattedMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {

    }
}
