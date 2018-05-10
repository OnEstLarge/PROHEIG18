package server;

import message.MessageType;
import netscape.javascript.JSObject;
import peer.PeerMessage;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerPeerToPeer {

    private HashMap<String, Socket> peopleInServ = new HashMap<String, Socket>();
    private HashMap<String, HashMap<String, JSObject>> groupeIntoNomJson = new HashMap<String, HashMap<String, JSObject>>();

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
                serverSocket = new ServerSocket(8080);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            while (!serverStopped) {
                try {
                    Socket client = serverSocket.accept();
                    new Thread(new server.ServerPeerToPeer.ServeurWorker(client)).start();
                   // new Thread(new updatePeaple()).start();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    /*
    private class updatePeaple implements Runnable {
        void updatePeaple(){}

        public void run(){
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            HashMap<String, Socket> copie = (HashMap<String, Socket>)peopleInServ.clone();
            for(String s : copie.keySet()) {
                Socket socket = copie.get(s);
                if(!socket.isConnected()){
                    peopleInServ.remove(s);
                }
            }
        }
    }
    */
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
                    if (type.equals(MessageType.HELO)) {
                        greetings(pm);
                    }

                    if (type.equals(MessageType.INFO)) {
                        giveInfoToDestinator(pm);
                    }

                    if (type.equals(MessageType.SMES) || type.equals(MessageType.SFIL) || type.equals(MessageType.INVT) || type.equals(MessageType.UPDT) ){
                        redirect(pm);
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
                    PeerMessage nok = new PeerMessage(MessageType.NOK,"AAAAAAA","AAAAAAA","AAAAAAA", ("").getBytes());
                    out.write(nok.getFormattedMessage());
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }



    }
}
