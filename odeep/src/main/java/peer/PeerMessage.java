package peer;
/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.DHR1Handler.java
 Auteur(s)   : Burgbacher Lionel, Jee Mathieu, Kopp Olivier, Piller Florent,
               Silvestri Romain, Schürch Loïc
 Date        : 15.03.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import com.sun.media.sound.InvalidFormatException;
import org.bouncycastle.util.Arrays;
import util.CipherUtil;

/**
 * Classe implémentant la structure définissant un message.
 *
 * Header Format:
 * TYPE,idGROUP=========,idFROM==========,idTO============,noPaquetMessageContent
 * TYPE         4 lettres maj
 * idGROUP      Nom du groupe, 16 chars max
 * idFROM       Source, 16 chars max
 * idTO         Dest,   16 chars max
 * noPaquet     numéro du paquet, int 8 digits
 */
public class PeerMessage {

    public static final int TYPE_LENGTH = 4;
    public static final int ID_GROUP_MIN_LENGTH = 6;
    public static final int ID_GROUP_MAX_LENGTH = 16;
    public static final int ID_MIN_LENGTH = 6;
    public static final int ID_MAX_LENGTH = 16;
    public static final int NO_PACKET_DIGITS = 8;
    public static final int BLOCK_SIZE = 32768;
    //evite le cas ou un message finit par le caracteres de padding
    public static final int FORCE_PADDING = 16;
    //padding eventuellement ajouté oar AES
    public static final int AES_PADDING = 16;
    public static final int HMAC_SIZE = CipherUtil.HMAC_SIZE;
    public static final int HEADER_SIZE = TYPE_LENGTH + ID_GROUP_MAX_LENGTH + 2 * ID_MAX_LENGTH + NO_PACKET_DIGITS + 4;
    public static final int MESSAGE_CONTENT_SIZE = BLOCK_SIZE - HEADER_SIZE - FORCE_PADDING - HMAC_SIZE - AES_PADDING;
    public static final int MESSAGE_WITH_PAD_SIZE = MESSAGE_CONTENT_SIZE + FORCE_PADDING + HMAC_SIZE + AES_PADDING;


    //padding de la forme "message@========"
    public static final char PADDING_START = '@';
    public static final char PADDING_SYMBOL = '=';

    /**
     * EN-TÊTE DU MESSAGE
     */
    private String type;            // type du message
    private String idGroup;         // nom du groupe
    private String idFrom;          // pseudo source
    private String idTo;            // pseudo destinataire
    private int noPacket;           // numéro du paquet

    /**
     * CONTENU DU MESSAGE
     */
    private byte[] messageContent;


    public PeerMessage(String type, String idGroup, String idFrom, String idTo, int noPacket, byte[] messageContent) throws IllegalArgumentException {
        synchronized (this) {
            if (!isValidTypeFormat(type)) {
                throw new IllegalArgumentException("Bad 'type' format");
            }

            if (!isValidIdFormat(idGroup, ID_GROUP_MIN_LENGTH, ID_GROUP_MAX_LENGTH)) {
                throw new IllegalArgumentException("Bad 'idGroup' format");
            }

            if (!isValidIdFormat(idFrom, ID_MIN_LENGTH, ID_MAX_LENGTH)) {
                throw new IllegalArgumentException("Bad 'idFrom' format");
            }

            if (!isValidIdFormat(idTo, ID_MIN_LENGTH, ID_MAX_LENGTH)) {
                throw new IllegalArgumentException("Bad 'idTo' format");
            }

            if (noPacket < 0) {
                throw new IllegalArgumentException("Invalid packet number (must be positiv)");
            }

            if (!isValidMessageContentFormat(messageContent)) {
                throw new IllegalArgumentException("Invalid message content (size must match block size (" + (MESSAGE_CONTENT_SIZE + HMAC_SIZE + AES_PADDING) + " bytes))");
            }

            this.type = type;
            this.idGroup = idGroup;
            this.idFrom = idFrom;
            this.idTo = idTo;
            this.noPacket = noPacket;
            this.messageContent = messageContent;
        }
    }

    public PeerMessage(String type, String idGroup, String idFrom, String idTo, byte[] message) {
        this(type, idGroup, idFrom, idTo, 0, message);
    }

    public PeerMessage(byte[] rawData) throws InvalidFormatException {
        synchronized (this) {
            if (rawData.length < HEADER_SIZE + 1) {
                throw new InvalidFormatException("incorrect message");
            }

            int index = 0;
            this.type = new String(CipherUtil.erasePadding(Arrays.copyOfRange(rawData, index, TYPE_LENGTH), PADDING_START));
            index += TYPE_LENGTH + 1;
            this.idGroup = new String(CipherUtil.erasePadding(Arrays.copyOfRange(rawData, index, index + ID_GROUP_MAX_LENGTH), PADDING_START));
            index += ID_GROUP_MAX_LENGTH + 1;
            this.idFrom = new String(CipherUtil.erasePadding(Arrays.copyOfRange(rawData, index, index + ID_MAX_LENGTH), PADDING_START));
            index += ID_MAX_LENGTH + 1;
            this.idTo = new String(CipherUtil.erasePadding(Arrays.copyOfRange(rawData, index, index + ID_MAX_LENGTH), PADDING_START));
            index += ID_MAX_LENGTH + 1;
            try {
                this.noPacket = Integer.parseInt(new String(Arrays.copyOfRange(rawData, index, index + NO_PACKET_DIGITS)));
            }
            catch (NumberFormatException e){
                System.err.println("altered packet, will be ignored");
            }
            index += NO_PACKET_DIGITS;
            this.messageContent = CipherUtil.erasePadding(Arrays.copyOfRange(rawData, index, rawData.length), PADDING_START);
        }
    }

    public PeerMessage(PeerMessage pm) {
        this(pm.getType(), pm.getIdGroup(), pm.getIdFrom(), pm.getIdTo(), pm.getNoPacket(), pm.getMessageContent());
    }

    /**
     * Vérifie que le format du type de message passé en argument soit correct.
     *
     * @param type type de message (Format: 4 lettres majuscules)
     * @return true si le format du type de message passé en argument est correct
     */
    public static boolean isValidTypeFormat(String type) {
        return type != null && type.length() == TYPE_LENGTH;
    }

    /**
     * Vérifie que le format du pseudo passé en argument soit correct.
     *
     * @param id pseudo
     * @return true si le format du pseudo passé en argument est correct
     */
    public static boolean isValidIdFormat(String id, int minLength, int maxLength) {
        return id != null && id.length() >= minLength && id.length() <= maxLength;
    }

    /**
     * Vérifie que le format du contenu du message passé en argument soit correct.
     *
     * @param messageContent contenu du message à envoyer
     * @return true si le format du contenu du message passé en argument est correct
     */
    public static boolean isValidMessageContentFormat(byte[] messageContent) {
        return messageContent != null && messageContent.length <= MESSAGE_CONTENT_SIZE + HMAC_SIZE + AES_PADDING;
    }

    /**
     * Vérifie si la string passée en argument est composée uniquement de majuscule.
     *
     * @param s
     * @return true si la string passée en argument est composée uniquement de majuscule
     */
    public static boolean isUpperCase(String s) {
        if (s == null | s == "") {
            return false;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (!Character.isUpperCase(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ajoute n zéros devant l'entier pour correspondre au format souhaité.
     *
     * @param number         entier à padder
     * @param numberOfDigits taille du padding
     * @return String paddé avec le nombre souhaité de zéros
     */
    public static String formatInt(int number, int numberOfDigits) {
        StringBuilder paddingRule = new StringBuilder();
        paddingRule.append("%0").append(numberOfDigits).append("d");

        return String.format(paddingRule.toString(), number);
    }

    /**
     * Ajoute du padding (si nécessaire) au texte.
     *
     * @param text        texte à "padder"
     * @param sizeWithPad taille totale du texte après ajout du padding
     * @param padSymbol   Symbole ajouté lors du padding
     * @return texte paddé
     */
    public static String addPadding(String text, int sizeWithPad, char padSymbol) {
        StringBuilder result = new StringBuilder(text);
        if (result.length() < sizeWithPad) {
            result.append(PADDING_START);
        }

        while (result.length() < sizeWithPad) {
            result.append(padSymbol);
        }

        return result.toString();
    }

    /**
     * ajout du padding à un tableau de byte
     *
     * @param data        tableau de byte à padder
     * @param sizeWithPad taille finale du tableau
     * @return le tableau paddé
     */
    public static byte[] addPadding(byte[] data, int sizeWithPad) {
        byte[] result = new byte[sizeWithPad];
        int index = 0;
        for (; index < data.length; index++) {
            result[index] = data[index];
        }
        if (index == sizeWithPad) {
            return result;
        }
        result[index] = PADDING_START;
        index++;
        for (; index < sizeWithPad; index++) {
            result[index] = PADDING_SYMBOL;
        }

        return result;
    }

    public String getType() {
        return type;
    }

    public String getIdFrom() {
        return idFrom;
    }

    public String getIdTo() {
        return idTo;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public byte[] getMessageContent() {
        return messageContent;
    }

    public int getNoPacket() {
        return noPacket;
    }

    /**
     * Retourne le message de format peer.PeerMessage et prêt à l'envoi
     *
     * @return peer.PeerMessage formatté
     */
    public synchronized byte[] getFormattedMessage() {
        synchronized (this) {
            byte[] toSend = null;
            do {
                toSend = new byte[HEADER_SIZE + MESSAGE_WITH_PAD_SIZE];
                int index = 0;

                for (int i = 0; i < TYPE_LENGTH; i++) {
                    toSend[index++] = type.getBytes()[i];
                }
                toSend[index++] = ",".getBytes()[0];

                byte[] group = addPadding(idGroup, ID_GROUP_MAX_LENGTH, PADDING_SYMBOL).getBytes();
                for (int i = 0; i < ID_GROUP_MAX_LENGTH; i++) {
                    toSend[index++] = group[i];
                }
                toSend[index++] = ",".getBytes()[0];

                byte[] from = addPadding(idFrom, ID_MAX_LENGTH, PADDING_SYMBOL).getBytes();
                for (int i = 0; i < ID_MAX_LENGTH; i++) {
                    toSend[index++] = from[i];
                }
                toSend[index++] = ",".getBytes()[0];

                byte[] to = addPadding(idTo, ID_MAX_LENGTH, PADDING_SYMBOL).getBytes();
                for (int i = 0; i < ID_MAX_LENGTH; i++) {
                    toSend[index++] = to[i];
                }
                toSend[index++] = ",".getBytes()[0];

                for (int i = 0; i < NO_PACKET_DIGITS; i++) {
                    toSend[index++] = formatInt(noPacket, NO_PACKET_DIGITS).getBytes()[i];
                }

                byte[] messageWithPad = addPadding(messageContent, MESSAGE_WITH_PAD_SIZE);

                for (int i = 0; i < MESSAGE_WITH_PAD_SIZE; i++) {
                    toSend[index++] = messageWithPad[i];
                }
            }
            //tant que le paquet est mal construit, on le reconstruit
            while (!(new String(toSend).substring(0, TYPE_LENGTH).equals(type)));
            return toSend;
        }
    }

}
