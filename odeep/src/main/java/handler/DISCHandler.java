package handler;

import User.Group;
import User.Person;
import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;
import main.Client;
import util.JSONUtil;
import Node.*;

import java.io.File;

public class DISCHandler implements MessageHandler{
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        String group = m.getIdGroup();
        File config = new File("./" + group);
        Group g = JSONUtil.parseJson(config.toString(), Group.class);
        for(int i = 0; i < g.getMembers().size(); i++){
            if(g.getMembers().get(i).equals(m.getIdTo())){
                g.getMembers().get(i).disconnect();
                Client.uploadJSON(JSONUtil.toJson(g),m.getIdGroup(), m.getIdTo());
            }
        }
    }
}
