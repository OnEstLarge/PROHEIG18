package handler;


import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;
import main.Client;
import util.InterfaceUtil;
import Node.FileSharingNode;

public class NFILHandler implements MessageHandler {
    @Override
    public void handleMessage(FileSharingNode n, PeerConnection c, PeerMessage m) {
        Client.downloadJSON(m.getIdGroup());
        InterfaceUtil.askFile(m.getIdGroup(), new String(m.getMessageContent()));
    }
}
