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
    private static byte[] redirectBuffer = new byte[4096];

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
        byte[] bufferIn = new byte[4096];
        byte[] bufferOut = new byte[4096];

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
                    while (read != 4096) {
                        int lu;
                        lu = in.read(bufferIn, 0, bufferIn.length);
                        read += lu;
                    }
                    read = 0;
                    PeerMessage pm = new PeerMessage(bufferIn);
                    String type = pm.getType();
                    if (MessageType.HELO.equals(type)) {
                        greetings(pm);

                    } else if (MessageType.BYE.equals(type)) {
                        bye(pm);

                    } else if (MessageType.USRV.equals(type)) {
                        validationUsername(pm);

                    } else if (MessageType.INFO.equals(type)) {
                        giveInfoToSender(pm);

                    } else if (MessageType.INVI.equals(type) || MessageType.INVK.equals(type) || MessageType.RFIL.equals(type) || MessageType.DHR1.equals(type) || MessageType.DHS2.equals(type) || MessageType.DHS1.equals(type) || MessageType.SFIL.equals(type) || MessageType.SMES.equals(type) || MessageType.UPDT.equals(type)) {//System.out.println("Message reçu " + pm.getType());
                        redirectBuffer = bufferIn.clone();
                        redirect(pm);

                    } else if (MessageType.NEWG.equals(type)) {
                        int ajout = DatabaseUtil.addGroupIfNotExists(pm.getIdGroup());
                        //System.out.println("Test : ajout " + ajout);
                        if (ajout == 1) {
                            //System.out.println("envoi du true");
                            out.write((new PeerMessage(pm.getType(), pm.getIdGroup(), pm.getIdFrom(), pm.getIdTo(), "true".getBytes())).getFormattedMessage());
                            out.flush();
                        } else {
                            //System.out.println("envoi du false");
                            out.write((new PeerMessage(pm.getType(), pm.getIdGroup(), pm.getIdFrom(), pm.getIdTo(), "false".getBytes())).getFormattedMessage());
                            out.flush();
                        }


                    } else if (MessageType.UPLO.equals(type)) {//On récupère la taille du JSON a download
                        String s = CipherUtil.erasePadding(new String(pm.getMessageContent()), PeerMessage.PADDING_START);
                        FileOutputStream fout = new FileOutputStream(new File("./groupsConfigs/" + pm.getIdGroup()));
                        int size = Integer.parseInt(s);
                        //System.out.println("Size =" + size);
                        int byteLu;
                        byte[] bufferTest = new byte[size];
                        byteLu = in.read(bufferTest, 0, size);
                        //System.out.println(byteLu);
                        size -= byteLu;
                        fout.write(bufferTest, 0, byteLu);
                        fout.flush();


                    } else if (MessageType.DOWN.equals(type)) {
                        System.out.println("download");
                        RandomAccessFile json = new RandomAccessFile("./groupsConfigs/" + pm.getIdGroup(), "r");

                        byte[] bufferJson = new byte[(int) json.length()];
                        json.readFully(bufferJson);
                        System.out.println("Download asked from " + pm.getIdFrom());
                        System.out.println("Download dest = " + pm.getIdTo());
                        System.out.println("PEOPLE IN SERV = ");
                        for (String p : peopleInServ.keySet()) {
                            System.out.println(p);
                        }

                        PeerMessage msg = new PeerMessage(MessageType.DOWN, pm.getIdGroup(), pm.getIdFrom(), pm.getIdTo(), ("" + (int) json.length()).getBytes());
                        out.write(msg.getFormattedMessage());
                        out.flush();
                        System.out.println("msg sent from server = " + new String(msg.getFormattedMessage()));
                        out.write(bufferJson);
                        out.flush();
                        System.out.println("bufferJson sent from server = " + new String(bufferJson));

                    } else {
                    }
                }
            } catch (IOException e) {
                //System.out.println(e.getMessage());
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
            //System.out.println("greetings " + pm.getIdFrom() + " " + new String(b));
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
