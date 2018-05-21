package handler;

import javafx.application.Platform;
import main.Client;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerMessage;
import Node.Node;
import util.CipherUtil;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class INVKHandler implements MessageHandler{

    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        final String groupId = m.getIdGroup();
        final String toSendTo = m.getIdFrom();

        //n.setKey(CipherUtil.generateKey(), groupId);

        RSAHandler RSA = new RSAHandler();
        try {
            RSA.setKeys();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        n.setTempRSAInfo(RSA);
        PeerMessage pm = new PeerMessage(MessageType.DHS1, groupId, Client.getUsername(), toSendTo, "".getBytes());
        c.sendMessage(pm);

    }
}