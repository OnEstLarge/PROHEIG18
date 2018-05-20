package handler;

import javafx.application.Platform;
import main.Client;
import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;
import Node.Node;

public class INVIHandler implements MessageHandler{

    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        final String groupId = m.getIdGroup();
        final String idFrom = m.getIdFrom();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Client.showAcceptInviteDialog(idFrom,groupId);
            }
        });
    }
}
