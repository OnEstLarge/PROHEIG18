package server;

import message.MessageType;
import netscape.javascript.JSObject;
import peer.PeerMessage;
import util.DatabaseUtil;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;

public class ServerPeerToPeer {

    private DatabaseUtil databaseUtil;
    private HashMap<String, Socket> peopleInServ = new HashMap<String, Socket>();
    private HashMap<String, HashMap<String, JSObject>> groupeIntoNomJson = new HashMap<String, HashMap<String, JSObject>>();

    public static void main(String[] args) {
        server.ServerPeerToPeer m = new server.ServerPeerToPeer();
        m.serveClient();
    }


    public void serveClient() {
        try {
            databaseUtil.initConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        new Thread(new server.ServerPeerToPeer.Receptioniste()).start();
    }


    private class Receptioniste implements Runnable {
        boolean serverStopped = false;

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8080);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            while (!serverStopped) {
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
                while ((in.read(bufferIn) != 1)) {
                    PeerMessage pm = new PeerMessage(bufferIn);
                    String type = pm.getType();
                    switch (type) {
                        case MessageType.HELO:
                            greetings(pm);
                            break;
                        case MessageType.BYE:
                            bye(pm);
                            break;
                        case MessageType.INFO:
                            giveInfoToDestinator(pm);
                            break;
                        case MessageType.INVI:
                        case MessageType.RFIL:
                        case MessageType.DHR1:
                        case MessageType.DHS2:
                        case MessageType.DHS1:
                        case MessageType.SFIL:
                        case MessageType.SMES:
                        case MessageType.UPDT:
                            redirect(pm);
                            break;

                        case MessageType.UPLO:
                            //Mettre la methode pour acceder au serveur
                            try {
                                databaseUtil.addGroupIfNotExists(pm.getIdGroup());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            //Lecture pour avoir le fichier JSON
                            for(){

                            }

                            databaseUtil.uploadJSON();//Le fichier JSON
                            break;

                        case MessageType.DOWN:
                            String s = databaseUtil.downloadJSON(pm.getIdGroup());
                            PeerMessage peerMessage = new PeerMessage("DOWN")

                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }


        private void greetings(PeerMessage pm) {
            peopleInServ.put(pm.getIdFrom(), clientToSever);
        }

        void giveInfoToDestinator(PeerMessage pm) {
            try {
                String to = pm.getIdTo();
                bufferOut = bufferIn.clone();
                BufferedOutputStream outTo = new BufferedOutputStream(peopleInServ.get(to).getOutputStream());
                outTo.write((clientToSever.getInetAddress().toString() + " port " + clientToSever.getPort()).getBytes());
                outTo.flush();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        void redirect(PeerMessage pm) {
            if (peopleInServ.containsKey(pm.getIdTo())) {
                try {
                    BufferedOutputStream toOut = new BufferedOutputStream(peopleInServ.get(pm.getIdTo()).getOutputStream());
                    toOut.write(pm.getFormattedMessage());
                    toOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    PeerMessage nok = new PeerMessage(MessageType.DISC,pm.getIdGroup(),pm.getIdFrom(),pm.getIdTo(), ("").getBytes());
                    out.write(nok.getFormattedMessage());
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        void bye(PeerMessage pm){
            if(peopleInServ.containsKey(pm.getIdFrom())){
                peopleInServ.remove(pm.getIdFrom());
            }
        }



    }
}
