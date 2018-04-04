/*
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerRelay {
    boolean serverShutdownRequested = false;
    ServerSocket receptionistSocket;
    InputStream is = null;
    OutputStream os = null;
    Socket clientSocket = null; // blocking call


    public ServerRelay(int port){
        try {
            receptionistSocket = new ServerSocket(80);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            ServerRelay s = new ServerRelay(80);
            s.listening();
        }catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
    public void listening() throws IOException {
        while (!serverShutdownRequested) {
            try {
                clientSocket = receptionistSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            initConnection();

            //receive file
            int i;
            byte buffer[] = new byte[4096];
            FileOutputStream out = new FileOutputStream("The.Walking.Dead.S08E12.HDTV.x264-SVA[ettv].mkv");
            while ((i = is.read(buffer)) != -1 ){
                out.write(buffer, 0, i);
            }
            out.close();


            closeConnection();


        }
        try {
            receptionistSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void initConnection(){
        try {
            is = clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
*/

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerRelay {

    public static void main(String[] args) {
        ServerRelay m = new ServerRelay();
        m.serveClient();
    }

    public void serveClient(){
        new Thread(new Receptioniste()).start();
    }


    private class Receptioniste implements Runnable{
        boolean serverStopped = false;
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try{
                serverSocket = new ServerSocket(80);
            }catch(IOException e){
                System.out.println(e.getMessage());
            }

            while(!serverStopped){
                System.out.println("En attente de connection");
                try {
                    Socket client = serverSocket.accept();
                    client.getInetAddress();
                    new Thread(new ServeurWorker(client)).start();
                }catch(IOException e){
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private class ServeurWorker implements Runnable{
        HashMap<String,String > peapleInServ;
        Socket clientToSever;
        Socket servToClient;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;

        public ServeurWorker(Socket clientSocket){
            try {
                this.clientToSever = clientSocket;
                in = new BufferedInputStream(clientSocket.getInputStream());
                out = new BufferedOutputStream(clientSocket.getOutputStream());

            }catch(IOException e){
                System.out.println(e.getMessage());
            }

        }
        @Override
        public void run() {
            redirectMessage(in);

        }

        void redirectMessage(BufferedInputStream in){
            byte[] cbuf = new byte[4096];

            try {
                while(in.read(cbuf) == 4096) {
                    String s = new String(cbuf);
                    s = s.substring(40, 56);
                    if (peapleInServ.containsKey(s)) {
                        String ip = peapleInServ.get(s);
                        try{
                            servToClient = new Socket(ip,80);
                        }catch (IOException e){
                            System.out.println(e.getMessage());
                        }
                        new Thread(new writeToClient(servToClient,cbuf)).start();                    }

                }
            }catch (IOException e){
                System.out.println(e.getMessage());
            }
        }


    }
    private class writeToClient implements Runnable{
        byte[] buff;
        Socket toClient = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;

        public writeToClient(Socket clientToWrite,byte[] buff){
            try {
                toClient = clientToWrite;
                in = new BufferedInputStream(clientToWrite.getInputStream());
                out = new BufferedOutputStream(clientToWrite.getOutputStream());
            }catch(IOException e){
                System.out.println(e.getMessage());
            }

            this.buff = buff.clone();

        }

        @Override
        public void run(){
            try {
                out.write(buff, 0, buff.length);
            }catch(IOException e){
                System.out.println(e.getMessage());
            }
        }
    }
}
