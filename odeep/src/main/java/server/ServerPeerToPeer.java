package server;

import peer.PeerMessage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerPeerToPeer {


    protected HashMap<String, Socket> peopleInServ = new HashMap<String, Socket>();

    public static void main(String[] args) {
        server.ServerPeerToPeer m = new server.ServerPeerToPeer();
        m.serveClient();
    }

    public void serveClient() {
        new Thread(new server.ServerPeerToPeer.Receptioniste()).start();
    }


    private class Receptioniste implements Runnable {
        boolean serverStopped = false;

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(80);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            while (!serverStopped) {
                System.out.println("En attente de connection");
                try {
                    Socket client = serverSocket.accept();
                    new Thread(new server.ServerPeerToPeer.ServeurWorker(client)).start();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }


    private class ServeurWorker implements Runnable {
        boolean stopConnection = false;
        Socket clientToSever;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        byte[] bufferIn = new byte[4096];
        byte[] bufferOut = new byte[4096];

        public ServeurWorker(Socket clientSocket) {
            try {
                this.clientToSever = clientSocket;
                in = new BufferedInputStream(clientSocket.getInputStream());
                out = new BufferedOutputStream(clientSocket.getOutputStream());
                out.flush();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                while ((in.read(bufferIn) != 1) && !stopConnection) {
                    PeerMessage pm = new PeerMessage(bufferIn);
                    String type = pm.getType();
                    if (type.equals("HELO")) {
                        greetings(pm);
                    }

                    if (type.equals("PUNC")) {
                        punch(pm);
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        private void greetings(PeerMessage pm) {
            peopleInServ.put(pm.getIdFrom(), clientToSever);

        }

        private void punch(PeerMessage pm) {
            if(peopleInServ.containsKey(pm.getIdTo())) {
                redirectMessage(pm);
                punchAdressTo(pm);
            }

        }

        void redirectMessage(PeerMessage pm) {
            System.out.println("Appelle de redirect");

            try {
                String to = pm.getIdTo();
                bufferOut = bufferIn.clone();
                BufferedOutputStream outTo = new BufferedOutputStream(peopleInServ.get(to).getOutputStream());
                outTo.write(clientToSever.getInetAddress().toString().getBytes());
                outTo.write(clientToSever.getPort());
                outTo.flush();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        void punchAdressTo(PeerMessage pm){
            Socket socketTo = peopleInServ.get(pm.getIdTo());
            String IP = socketTo.getInetAddress().toString();
            int port = socketTo.getPort();
            try {
                out.write(IP.getBytes());
                out.write(port);
                out.flush();
            }catch(IOException e){
                System.out.println(e.getMessage());
            }
        }
    }


}
