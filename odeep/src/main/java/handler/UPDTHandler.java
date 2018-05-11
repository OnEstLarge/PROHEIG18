package handler;

import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;
import main.Client;

public class UPDTHandler implements MessageHandler{
    @Override
    public void handleMessage(PeerConnection c, PeerMessage m) {
        Client.downloadJSON(m.getIdGroup());
    }
}
