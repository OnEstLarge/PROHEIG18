import peer.PeerMessage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TestRelay {

    public static void main(String[] args) {
        Socket clientSocket = null;
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            clientSocket = new Socket("206.189.49.105", 8080);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            //System.out.println(in.readLine());

            System.out.println("Sending to serv");
            out.println("HELO-schurch-"+ clientSocket.getLocalAddress() + "-" + 4444);
            out.flush();
            System.out.println("sent and response: " + in.readLine());
            //System.out.println(in.readLine());

            //PeerMessage punchInfo = new PeerMessage("PUNC", "AAAAAAAA", "schurch", "florent", "info I want".getBytes());
            System.out.println("Request info");

            out.println("PUNC-schurch-kirikou");
            out.flush();

            String line = "";
            while((line = in.readLine()) != null){
                System.out.println(line);
            }
            clientSocket = new Socket("206.189.49.105", 8080);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println(e);
            }

            try {
                in.close();
            } catch (IOException e) {
                System.out.println(e);
            }

            out.close();

        }
    }
}
