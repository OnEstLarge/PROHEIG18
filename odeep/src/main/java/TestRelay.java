import peer.PeerConnection;
import peer.PeerHandler;
import peer.PeerMessage;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TestRelay {

    public static void main(String[] args) {
        Socket clientSocket = null;
        BufferedReader in = null;
        PrintWriter out = null;

        new Thread(new Runnable(){
                public void run() {
                    ServerSocket serverSocket = null;
                    Socket clientSocket = null;

                    try {
                        serverSocket = new ServerSocket(4444);
                        while (true) {
                            //socket wait for connection
                            try {
                                clientSocket = serverSocket.accept();

                                System.out.println("IL EST LAAAAAAAA");
                                clientSocket.close();
                            } catch (IOException ex) {
                                //TODO
                            }
                        }

                    } catch (IOException ex) {}
                }
        }).start();

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

            boolean shutdown = false;
            String line = "";
            while(!shutdown & (line = in.readLine()) != null){
                System.out.println(line);
                String[] rcv = line.split("-");
                if(rcv[0].equals("PUNC")) {
                    String pseudo = rcv[1];
                    String ip = rcv[2].replaceAll("/", "");
                    int port = Integer.parseInt(rcv[3]);
                    System.out.println("trying to punch on :" + pseudo + " ip: "+ ip+ " port: "+ port);
                    for(int i = 0; i < 4; ++i) {
                        try {
                            System.out.println("trying...");
                            Socket client = new Socket(ip, port);
                            PeerConnection c = new PeerConnection(client, false);
                            PeerMessage m = new PeerMessage("SMES", "AAAAAAAA", "schurch", pseudo, "JE TE PUNCH".getBytes());
                            c.sendMessage(m);
                            c.close();
                            try {
                                new Thread().sleep(100);
                            }catch(InterruptedException e) {}
                        }catch(IOException e) {}
                    }
                }
            }





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
