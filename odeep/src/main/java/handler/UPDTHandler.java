package handler;

import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;
import main.Client;
import Node.*;

public class UPDTHandler implements MessageHandler{
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        Client.downloadJSON(m.getIdGroup());
    }
}
