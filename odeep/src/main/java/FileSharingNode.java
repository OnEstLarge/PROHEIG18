import java.io.*;
import java.net.Socket;

public class FileSharingNode extends Node {
    private MessageType messageType;

    public FileSharingNode(PeerInformations myInfos) {
        super(myInfos);
        try {
            test();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFileToPeer(File file, PeerInformations destination) throws IOException {
        byte buffer[] = new byte[4096];
        FileInputStream in = new FileInputStream(file);
        BufferedOutputStream out = new BufferedOutputStream(/*destination.getSocket()*/ new Socket("127.0.0.1", 80).getOutputStream());
            int j;
            int i = 0;
            while ((j = in.read(buffer)) != -1){
                out.write(buffer, 0, j);
                i++;
                System.out.println(((i+1)*buffer.length)/(double)file.length()*100 % 1 + "%");
            }

        out.close();
        in.close();
    }

    public void test() throws IOException {
        File[] filesList = new File(".").listFiles();
        for(File f : filesList){
            if(f.isFile()){
                System.out.println(f.getName());
            }
        }
        sendFileToPeer(new File("./testfile"), new PeerInformations("test","127.0.0.1", 80));

    }


}
