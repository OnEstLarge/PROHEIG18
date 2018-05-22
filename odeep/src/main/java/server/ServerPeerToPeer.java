package server;

import message.MessageType;
import peer.PeerMessage;
import util.CipherUtil;
import util.DatabaseUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;

public class ServerPeerToPeer {

    private DatabaseUtil databaseUtil;
    private HashMap<String, String> clientIPPrivee = new HashMap<String, String>();
    private HashMap<String, Socket> peopleInServ = new HashMap<String, Socket>();
    private static byte[] redirectBuffer = new byte[PeerMessage.BLOCK_SIZE];

    public static void main(String[] args) {
        server.ServerPeerToPeer m = new server.ServerPeerToPeer();
        m.serveClient();
    }


    public void serveClient() {
        databaseUtil = new DatabaseUtil();
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
                //System.out.println(e.getMessage());
            }

            while (!serverStopped) {
                try {
                    Socket client = serverSocket.accept();
                    new Thread(new server.ServerPeerToPeer.ServeurWorker(client)).start();
                } catch (IOException e) {
                    //System.out.println(e.getMessage());
                }
            }
        }
    }

    private class ServeurWorker implements Runnable {
        Socket clientToSever;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        byte[] bufferIn = new byte[PeerMessage.BLOCK_SIZE];
        byte[] bufferOut = new byte[PeerMessage.BLOCK_SIZE];

        public ServeurWorker(Socket clientSocket) {
            try {
                this.clientToSever = clientSocket;
                in = new BufferedInputStream(clientSocket.getInputStream());
                out = new BufferedOutputStream(clientSocket.getOutputStream());
                out.flush();
            } catch (IOException e) {
                //System.out.println(e.getMessage());
            }
        }

        @Override
        public void run() {

            try {

                while (true) {
                    int read = 0;
                    while (read != PeerMessage.BLOCK_SIZE) {
                        int lu;
                        lu = in.read(bufferIn, 0, bufferIn.length);
                        read += lu;
                    }
                    read = 0;
                    System.out.println("\n---\n---BufferIN = ");
                    System.out.println(new String(bufferIn) + "\n---\n\n");
                    PeerMessage pm = new PeerMessage(bufferIn);
                    String type = pm.getType();
                    switch (type) {
                        case MessageType.HELO:
                            greetings(pm);
                            break;
                        case MessageType.BYE:
                            bye(pm);
                            break;
                        case MessageType.USRV:
                            validationUsername(pm);
                            break;
                        case MessageType.INFO:
                            giveInfoToSender(pm);
                            break;
                        case MessageType.INVI:
                        case MessageType.INVK:
                        case MessageType.RFIL:
                        case MessageType.DHR1:
                        case MessageType.DHS2:
                        case MessageType.DHS1:
                        case MessageType.SFIL:
                        case MessageType.SMES:
                        case MessageType.UPDT:
                            redirectBuffer = bufferIn.clone();
                            redirect(pm);
                            break;

                        case MessageType.NEWG:
                            System.out.println("Creation Group, FROM: " + pm.getIdFrom());
                            int ajout = DatabaseUtil.addGroupIfNotExists(pm.getIdGroup());

                            if (ajout == 1) {
                                System.out.println("Group name available: " + pm.getIdGroup());
                                out.write((new PeerMessage(pm.getType(), pm.getIdGroup(), pm.getIdFrom(), pm.getIdTo(), "true".getBytes())).getFormattedMessage());
                                out.flush();
                            } else {
                                System.out.println("Group name unavailable: " + pm.getIdFrom());
                                out.write((new PeerMessage(pm.getType(), pm.getIdGroup(), pm.getIdFrom(), pm.getIdTo(), "false".getBytes())).getFormattedMessage());
                                out.flush();
                            }

                            break;

                        case MessageType.UPLO:
                            System.out.println("Upload, FROM: " + pm.getIdFrom() + ", " + "TO: " + pm.getIdTo());

                            //On récupère la taille du JSON a download
                            FileOutputStream fout = new FileOutputStream(new File("./groupsConfigs/" + pm.getIdGroup()));

                            fout.write(pm.getMessageContent(), 0, pm.getMessageContent().length);
                            fout.flush();
                            fout.close();

                            break;

                        case MessageType.DOWN:
                            System.out.println("Download FROM: " + pm.getIdFrom() + ", " + "TO: " + pm.getIdTo());
                            RandomAccessFile json = new RandomAccessFile("./groupsConfigs/" + pm.getIdGroup(), "r");

                            byte[] bufferJson = new byte[(int) json.length()];
                            json.readFully(bufferJson);

                            PeerMessage jsonPm = new PeerMessage(pm.getType(), pm.getIdGroup(), pm.getIdFrom(), pm.getIdTo(), bufferJson);
                            out.write(jsonPm.getFormattedMessage());
                            out.flush();

                            break;

                        default:
                            break;
                    }
                }
            } catch (IOException e) {
                //System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }


        private void validationUsername(PeerMessage pm) {
            int validation = DatabaseUtil.addUserIfNotExists(pm.getIdFrom());
            //System.out.println("validation ngggggg");
            try {
                if (validation == 1) {
                    out.write(new PeerMessage(pm.getType(), pm.getIdGroup(), pm.getIdFrom(), pm.getIdTo(), "true".getBytes()).getFormattedMessage());
                } else {
                    out.write(new PeerMessage(pm.getType(), pm.getIdGroup(), pm.getIdFrom(), pm.getIdTo(), "false".getBytes()).getFormattedMessage());
                }
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void greetings(PeerMessage pm) {
            byte[] b = CipherUtil.erasePadding(pm.getMessageContent(), PeerMessage.PADDING_START);
            System.out.println("Greetings, " + pm.getIdFrom());
            peopleInServ.put(pm.getIdFrom(), clientToSever);
            clientIPPrivee.put(pm.getIdFrom(), new String(b));
        }

        void giveInfoToSender(PeerMessage pm) {
            //System.out.println("giveInfo " + pm.getIdFrom() + " " + pm.getType());
            if (clientIPPrivee.containsKey(pm.getIdTo())) {
                try {

                    out.write(new PeerMessage(pm.getType(), pm.getIdGroup(), pm.getIdFrom(), pm.getIdTo(), clientIPPrivee.get(pm.getIdTo()).getBytes()).getFormattedMessage());
                    out.flush();
                } catch (IOException e) {
                    //System.out.println(e.getMessage());
                }
            } else {
                try {
                    out.write(new PeerMessage(pm.getType(), pm.getIdGroup(), pm.getIdFrom(), pm.getIdTo(), "".getBytes()).getFormattedMessage());
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void redirect(PeerMessage pm) {
            if (peopleInServ.containsKey(pm.getIdTo())) {
                try {
                    BufferedOutputStream toOut = new BufferedOutputStream(peopleInServ.get(pm.getIdTo()).getOutputStream());
                    toOut.write(redirectBuffer);
                    toOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (pm.getType().equals(MessageType.INVI)) {
                try {
                    PeerMessage nok = new PeerMessage(MessageType.DISC, pm.getIdGroup(), pm.getIdFrom(), pm.getIdTo(), ("").getBytes());
                    out.write(nok.getFormattedMessage());
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        void bye(PeerMessage pm) {
            if (peopleInServ.containsKey(pm.getIdFrom())) {
                peopleInServ.remove(pm.getIdFrom());
                if (clientIPPrivee.containsKey(pm.getIdFrom()))
                    clientIPPrivee.remove(pm.getIdFrom());
            }
        }

    }
}
