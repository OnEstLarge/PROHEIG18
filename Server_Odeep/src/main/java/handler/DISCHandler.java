package handler;

import User.Group;
import User.Person;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerMessage;
import util.DatabaseUtil;
import util.JSONUtil;

import java.io.File;

public class DISCHandler implements MessageHandler{
    @Override
    public void handleMessage(PeerConnection c, PeerMessage m) {
        String group = m.getIdGroup();
        File config = new File("./" + group);
        Group g = JSONUtil.parseJson(config.toString(), Group.class);
        for(int i = 0; i < g.getMembers().size(); i++){
            if(g.getMembers().get(i).equals(m.getIdTo())){
                g.getMembers().get(i).disconnect();
                DatabaseUtil.uploadJSON(JSONUtil.toJson(g),m.getIdGroup());
            }
        }
    }
}
