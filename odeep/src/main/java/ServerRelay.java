import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerRelay {

    private final static int SERVER_PORT = 8080;

    protected HashMap<String, InfoUser> connectedUsers = new HashMap<String, InfoUser>();

    public static void main(String[] args) {
        ServerRelay s = new ServerRelay();
        s.serveClient();
        
    }

    public void serveClient() {
        new Thread(new ServerRelay.ReceptionistWorker()).start();
    }

    private class InfoUser {
        private String IPLocal;
        private String IPPublic;
        private int portLocal;
        private int portPublic;
        BufferedReader reader;
        PrintWriter writer;

        public InfoUser(String localIP, int localPort, String publicIP, int publicPort, BufferedReader reader, PrintWriter writer) {
            IPLocal = localIP;
            portLocal = localPort;
            IPPublic = publicIP;
            portPublic = publicPort;
            this.reader = reader;
            this.writer = writer;
        }

        public String getLocalIP()  { return IPLocal;}
        public String getPublicIP() {return IPPublic;}
        public int getLocalPort()   {return portLocal;}
        public int getPublicPort()  {return portPublic;}
        public BufferedReader getReader(){ return reader;}
        public PrintWriter getWriter() {return writer;}
        
    }

    private class ReceptionistWorker implements Runnable {
        boolean serverStopped = false;

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            while (!serverStopped) {
                System.out.println("En attente de connection...");
                try {
                    Socket client = serverSocket.accept();
                    System.out.println("Connection en traitement...");
                    new Thread(new ServeurWorker(client)).start();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }


    private class ServeurWorker implements Runnable {
        boolean stopConnection = false;
        Socket clientSocket;
        BufferedReader reader;
        PrintWriter writer;

        public ServeurWorker(Socket clientSocket) {
            try {
                this.clientSocket = clientSocket;
                OutputStream os = this.clientSocket.getOutputStream();
                InputStream is = this.clientSocket.getInputStream();

                reader = new BufferedReader(new InputStreamReader(is));
                writer = new PrintWriter(new OutputStreamWriter(os));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        private void close(){
            if(writer != null){
                writer.close();
            }
            if(reader != null){
                try{
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(clientSocket != null){
            try{
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            try {
                String line = null;
                while ((!stopConnection) && (line = reader.readLine()) != null) {

System.out.println("To SPLIT---> " + line);
                    String[] received = line.split("-");

                    String type = received[0];
                    
                    /* On peut recevoir:
                     *
                     *   HELO: "HELO-pseudo-ip locale-port local" -> ajouter cet utilisateur à la liste des personnes connues
                     *   
                     *   BYES: "BYES-pseudo" -> on enlève cet utilisateur
                     *
                     *   PUNC: "PUNC-my pseudo - pseudo I want to connect to (B)" 
                     *         -> Envoie au sender (A) les infos de B -> "pseudo-ip publique-port public-ip locale-port local?"
                     *            Informe B que A veut se connecter avec lui en lui envoyant les info de A -> "pseudo-ip publique-port public-ip locale-port local?"
                     */
                    if (type.equals("HELO")) {
                        stopConnection = !greetings(received);
                    } else if(type.equals("BYES")) {
                        bye(received);
                        stopConnection = true;
                    } else if (type.equals("PUNC")) {
                        punch(received);
                    } else {
                        stopConnection = true;
                    }
                }
                //close the connection
                close();

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        private boolean greetings(String[] received) {

            String pseudo = null;
            String IPLocal = null;
            int portLocal;
            if(received.length != 4 & (pseudo = received[1])  == null & (IPLocal = received[2]) == null) {
                return false;
            } else {
                try{
                    portLocal = Integer.parseInt(received[3]);
                } catch(NumberFormatException e) {
                    System.out.println(e.getMessage());
                    return false;
                }
                System.out.println("Greetings " + pseudo);
                connectedUsers.put(pseudo,  new InfoUser(IPLocal, portLocal, clientSocket.getInetAddress().toString(), 
                                                         clientSocket.getPort(), reader, writer));

                writer.println("Greetings " + pseudo);
                writer.flush();

                System.out.println("Connected users: " + connectedUsers);
                return true;
            }
        }

        private void bye(String[] received) {
            String pseudo = null;
            if(received.length != 2 & (pseudo = received[1]) == null) {
                System.out.println("Couldn't remove this peer...");
                //BYES impossible
                writer.println("BYES-NOK");
                writer.flush();
            } else {
                //BYES ok
                writer.println("BYES-OK");
                writer.flush();
                connectedUsers.remove(pseudo);
            }
        }

        private void punch(String[] received) {

            String from = null;
            String to = null;
            if(received.length != 3 & (from = received[1]) == null & (to = received[2]) == null) {

                System.out.println("Invalid pseudo, cannot punch");

                //PUNCH impossible
                writer.println("PUNC-NOK");
                writer.flush();

            }

            System.out.println("Initating TCP punching between " + from + " and " + to + "...");
            if(! connectedUsers.containsKey(to)) {
                System.out.println("User " + to + " is not availaible");

                //WRITE TO SENDER THAT USER IS NOT CONNECTED
                writer.println("PUNC-NOK");
                writer.flush();
            } else {

                System.out.println("Informing receiver " + to + "...");
                giveInfoToDestinator(from, to);

                System.out.println("Sending informations to sender " + from);
                giveInfoToSender(from, to);

            }

        }

        void giveInfoToDestinator(String from, String to) {
            
            PrintWriter w = connectedUsers.get(to).getWriter();

            String localIP = connectedUsers.get(from).getLocalIP();
            String publicIP = connectedUsers.get(from).getPublicIP();
            int localPort = connectedUsers.get(from).getLocalPort();
            int publicPort = connectedUsers.get(from).getPublicPort();

            w.println("PUNC-"+from+"-"+publicIP+"-"+publicPort+"-"+localIP+"-"+localPort);
            w.flush();

        }

        void giveInfoToSender(String from, String to){

            String localIP = connectedUsers.get(to).getLocalIP();
            String publicIP = connectedUsers.get(to).getPublicIP();
            int localPort = connectedUsers.get(to).getLocalPort();
            int publicPort = connectedUsers.get(to).getPublicPort();

            writer.println("PUNC-"+to+"-"+publicIP+"-"+publicPort+"-"+localIP+"-"+localPort);
            writer.flush();
        }
    }


}
