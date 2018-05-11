package server;

import com.google.gson.JsonObject;
import message.MessageType;
import netscape.javascript.JSObject;
import peer.PeerMessage;
import util.CipherUtil;
import util.DatabaseUtil;
import util.JSONUtil;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;

public class ServerPeerToPeer {

    private DatabaseUtil databaseUtil;
    private HashMap<String,String> clientIPPrivee = new HashMap<String, String>();
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

                while (true) {
                    int read = 0;
                    while(read != 4096) {
                        int lu;
                        lu = in.read(bufferIn,0,bufferIn.length);
                        read += lu;
                    }
                    read = 0;
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
                            giveInfoToSender(pm);
                            break;
                        case MessageType.INVI:
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

                        case MessageType.UPLO:
                            //Mettre la methode pour acceder au serveur
                            DatabaseUtil.addGroupIfNotExists(pm.getIdGroup());

                            //On récupère la taille du JSON a download
                            String s = CipherUtil.erasePadding(new String(pm.getMessageContent()),PeerMessage.PADDING_START);
                            System.out.println(new String(pm.getMessageContent()));
                            
                            /*
                            int taille = Integer.parseInt(s);
                            byte[] json = new byte[taille];
                            int byteLu = 0;
                            byte[] testBuffer = new byte[4096];
                            //Lecture pour avoir le fichier JSON
                            while(byteLu != taille && in.read(bufferIn,0,4096) != -1){
                                PeerMessage peerMessage = new PeerMessage(bufferIn);
                                System.out.println("Recu : " + peerMessage.getType() + " " + new String(peerMessage.getMessageContent()));
                                byte[] content = CipherUtil.erasePadding(peerMessage.getMessageContent(),PeerMessage.PADDING_START);
                                for(int i = 0 ; i < content.length;++i)
                                    json[byteLu + i] = content[i];
                                byteLu += content.length;
                            }

                            String stringJSON = new String(json);

                            */

                            break;

                        case MessageType.DOWN:
                            String stringJSON = DatabaseUtil.downloadJSON(pm.getIdGroup());
                            int nbpack = (int) Math.ceil(stringJSON.length()/PeerMessage.MESSAGE_CONTENT_SIZE);
                            String size = "" + nbpack;
                            out.write(new PeerMessage(MessageType.DOWN,pm.getIdGroup(),pm.getIdFrom(),pm.getIdTo(),size.getBytes()).getFormattedMessage());
                            if(nbpack == 1){
                                out.write(new PeerMessage(MessageType.DOWN,pm.getIdGroup(),pm.getIdFrom(),pm.getIdTo(),stringJSON.getBytes()).getFormattedMessage());
                            }else{
                                for(int i = 0 ; i < nbpack; ++i){
                                    out.write(new PeerMessage(MessageType.DOWN,pm.getIdGroup(),pm.getIdFrom(),pm.getIdTo(),
                                            stringJSON.substring(i*PeerMessage.MESSAGE_CONTENT_SIZE,(i+1)*PeerMessage.MESSAGE_CONTENT_SIZE).getBytes()).getFormattedMessage());
                                }
                            }

                        default:
                            break;

                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }


        private void greetings(PeerMessage pm) {
           byte[] b = CipherUtil.erasePadding(pm.getMessageContent(),PeerMessage.PADDING_START);


            System.out.println("greetings " + pm.getIdFrom() + " " + new String(b));
            peopleInServ.put(pm.getIdFrom(), clientToSever);
            clientIPPrivee.put(pm.getIdFrom(), new String(b));
        }

        void giveInfoToSender(PeerMessage pm) {
            System.out.println("giveInfo " +  pm.getIdFrom() + " " + pm.getType()) ;
            if(clientIPPrivee.containsKey(pm.getIdTo())) {
                try {

                    out.write(new PeerMessage(pm.getType(),pm.getIdGroup(),pm.getIdFrom(),pm.getIdTo(),clientIPPrivee.get(pm.getIdTo()).getBytes()).getFormattedMessage());
                    out.flush();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }else{
                try {
                    out.write(new PeerMessage(pm.getType(),pm.getIdGroup(),pm.getIdFrom(),pm.getIdTo(),"".getBytes()).getFormattedMessage());
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
                if(clientIPPrivee.containsKey(pm.getIdFrom()))
                    clientIPPrivee.remove(pm.getIdFrom());
            }
        }

    }
}
