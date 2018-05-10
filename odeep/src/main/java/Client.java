import handler.BYEHandler;
import handler.RSAHandler;
import handler.SFILHandler;
import handler.SMESHandler;
import message.MessageType;
import peer.PeerMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket clientSocket;
    private BufferedInputStream in;
    private BufferedOutputStream out;

    private ServerSocket serverSocket;
    private byte cbuf[] = new byte[4096];
    private byte cbuf1[] = new byte[4096];

    public static void main(String[] args) {
        Client c = new Client();
        c.initConnections("206.189.49.105", 8080);;
    }

    //initialise la connection avec le serveur et de lancer le server d'écoute du client
    public void initConnections(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in = new BufferedInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out = new BufferedOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new Server()).start();
        new Thread(new ConnectionServer()).start();
    }

    //initialise la connection au serveur avec une thread pour la lecture et l'autre pour l'écriture
    private class ConnectionServer implements Runnable{
        public ConnectionServer(){
        }
        public void run(){
            new Thread(new ReadFromServer()).start();
            new Thread(new WriteToServer()).start();
        }

    }

    //Classe permettant de threader la lecture des packets server
    private class ReadFromServer implements Runnable{
        public void run(){
            int read;
            byte[] buffer = new byte[4096];

            try {
                while ((read = in.read(buffer)) != -1) {
                    PeerMessage pm = new PeerMessage(buffer);
                    String type = pm.getType();
                    switch (type) {

                        case MessageType.SFIL:
                            new SFILHandler().handleMessage();
                            break;

                        case MessageType.SMES:
                            new SMESHandler().handleMessage();
                            break;

                        case MessageType.:
                            break;
                        case "BYE":
                            new BYEHandler().handleMessage();
                            break;

                        default :
                            System.out.println("Message pas reconnu");

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

                try {
                    out.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

    //Classe permettant de threader l'envoie de packet au server
    public class WriteToServer implements Runnable{

        //TODO faire les trucs de l'inteface graphique ici
        public void run(){

        }
    }

    //Server qui permet de reçevoir les communications de l'extérieur
    private class Server implements Runnable {
        boolean serverStopped = false;
        ServerSocket serverSocket = null;

        public Server(){
            try {
                serverSocket = new ServerSocket(8080);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        }

        @Override
        public void run() {
            while (!serverStopped) {
                System.out.println("En attente de connection");
                try {
                    Socket client = serverSocket.accept();
                    new Thread(new sendFileClass(client)).start();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    //Permet d'envoyer un fichier diretement vers l'autre personne
    private class sendFileClass implements Runnable {
        private Socket socketToSend;
        private BufferedInputStream inToSend;
        private BufferedOutputStream outToSend;
        byte[] buffer = new byte[4096];

        private sendFileClass(Socket toSend) {
            try {
                socketToSend = toSend;
                inToSend = new BufferedInputStream(toSend.getInputStream());
                outToSend = new BufferedOutputStream(toSend.getOutputStream());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        public void run() {
            try {
                //on lit le nom du fichier voulu
                int read = inToSend.read(buffer);
                String s = new String(buffer);
                s = s.substring(read);

                //on envoie le fichier voulu
                try {
                    FileInputStream file = new FileInputStream(s);
                    while ((read = file.read(buffer)) != -1) {
                        outToSend.write(buffer, 0, read);
                    }
                }catch (FileNotFoundException e){
                    System.out.println(e.getMessage());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                try {
                    inToSend.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    outToSend.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socketToSend.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
