package handler;

import Node.Node;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerMessage;
import util.CipherUtil;

public class DHR1Handler implements MessageHandler{
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        RSAHandler RSA = n.getTempRSAInfo();
        if (RSA != null) {
            byte[] foreignKey = CipherUtil.erasePadding(m.getMessageContent(), PeerMessage.PADDING_START);
            byte[] encryptedKey = CipherUtil.RSAEncrypt(CipherUtil.byteToPublicKey(foreignKey), n.getKey(m.getIdGroup()));
            PeerMessage message = new PeerMessage(MessageType.DHS2, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), encryptedKey);
            c.sendMessage(message);
            c.close();
            n.setTempRSAInfo(null);
        }
    }
}
