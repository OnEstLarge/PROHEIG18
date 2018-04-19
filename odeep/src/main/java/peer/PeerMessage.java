package peer;/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : peer.PeerHandler.java
 Auteur(s)   : Kopp Olivier, Jee Mathieu, Schürch Loïc
 Date        : 15.03.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import com.sun.media.sound.InvalidFormatException;
import org.bouncycastle.util.Arrays;

/**
 * Header Format:
 *
 * TYPE,idGROUP=========,idFROM==========,idTO============,noPaquet,MessageContentOn4032bytes
 * TYPE         4 lettres maj
 * idGROUP      Nom du groupe, 16 chars max
 * idFROM       Source, 16 chars max
 * idTO         Dest,   16 chars max
 * noPaquet     numéro du paquet, int 8 digits (max 400Go)
 *
 * bytes total header  = 4+1+16+1+16+1+8+1 = 64 bytes
 * bytes total message = 4096 - 64 = 4032 bytes
 *
 */
public class PeerMessage {

    public static final int  TYPE_LENGTH          =      4;
    public static final int  ID_GROUP_MIN_LENGTH  =      6;
    public static final int  ID_GROUP_MAX_LENGTH  =     16;
    public static final int  ID_MIN_LENGTH        =      6;
    public static final int  ID_MAX_LENGTH        =     16;
    public static final int  NO_PACKET_DIGITS     =      8;
    public static final int  BLOCK_SIZE           =   4096;
    public static final int  HEADER_SIZE          = TYPE_LENGTH + ID_GROUP_MAX_LENGTH + 2 * ID_MAX_LENGTH + NO_PACKET_DIGITS + 4;
    public static final int  MESSAGE_CONTENT_SIZE = BLOCK_SIZE - HEADER_SIZE;

    public static final char PADDING_SYMBOL       = '=';

    /**
     * EN-TÊTE DU MESSAGE
     */
    private String type;            // type du message
    private String idGroup;         // nom du groupe
    private String idFrom;          // pseudo source
    private String idTo;            // pseudo destinataire
    private int    noPacket;        // numéro du paquet

    /**
     * CONTENU DU MESSAGE
     */
    private byte[] messageContent;


    public PeerMessage(String type, String idGroup, String idFrom, String idTo, int noPacket, byte[] messageContent) throws IllegalArgumentException{

        if(!isValidTypeFormat(type)) {
            throw new IllegalArgumentException("Bad 'type' format");
        }

        if(!isValidIdFormat(idGroup, ID_GROUP_MIN_LENGTH, ID_GROUP_MAX_LENGTH)) {
            throw new IllegalArgumentException("Bad 'idGroup' format");
        }

        if(!isValidIdFormat(idFrom, ID_MIN_LENGTH, ID_MAX_LENGTH)) {
            throw new IllegalArgumentException("Bad 'idFrom' format");
        }

        if(!isValidIdFormat(idTo, ID_MIN_LENGTH, ID_MAX_LENGTH)) {
            throw new IllegalArgumentException("Bad 'idTo' format");
        }

        if(noPacket < 0) {
            throw new IllegalArgumentException("Invalid packet number (must be positiv)");
        }

        if(!isValidMessageContentFormat(messageContent)) {
            throw new IllegalArgumentException("Invalid message content (size must match block size (" + MESSAGE_CONTENT_SIZE + " bytes))");
        }

        this.type           = type;
        this.idGroup        = idGroup;
        this.idFrom         = idFrom;
        this.idTo           = idTo;
        this.noPacket       = noPacket;
        this.messageContent = messageContent;
    }

    public PeerMessage(String type, String idGroup, String idFrom, String idTo, byte[] message) {
        this(type, idGroup, idFrom, idTo, 0, message);
    }

    public PeerMessage(byte[] rawData) throws InvalidFormatException {
        if(rawData.length < HEADER_SIZE + 1){
            throw new InvalidFormatException("incorrect message");
        }
        //System.out.println(new String(rawData));

        int index = 0;
        final String PAD = "" + PADDING_SYMBOL;
        this.type           = new String(Arrays.copyOfRange(rawData,index, TYPE_LENGTH)).replaceAll(PAD, "");
        //System.out.println("constr type " + this.type);
        index += TYPE_LENGTH+1;
        this.idGroup        = new String(Arrays.copyOfRange(rawData,index, index + ID_GROUP_MAX_LENGTH)).replaceAll(PAD, "");
        //System.out.println("constr idGroup " + this.idGroup);
        index += ID_GROUP_MAX_LENGTH+1;
        this.idFrom         = new String(Arrays.copyOfRange(rawData,index , index + ID_MAX_LENGTH)).replaceAll(PAD, "");
        //System.out.println("constr idfrom " + this.idFrom);
        index += ID_MAX_LENGTH+1;
        this.idTo           = new String(Arrays.copyOfRange(rawData,index, index + ID_MAX_LENGTH)).replaceAll(PAD, "");
        //System.out.println("constr idto " + this.idTo);
        index += ID_MAX_LENGTH+1;
        this.noPacket       = Integer.parseInt(new String(Arrays.copyOfRange(rawData,index, index +NO_PACKET_DIGITS)).replaceAll(PAD, ""));
        //System.out.println("constr noPa " + this.noPacket);
        index += NO_PACKET_DIGITS+1;
        this.messageContent = Arrays.copyOfRange(rawData,index, rawData.length);
        //System.out.println(new String(messageContent));
    }

    /**
     * Vérifie que le format du type de message passé en argument soit correct.
     *
     * @param type  type de message (Format: 4 lettres majuscules)
     * @return      true si le format du type de message passé en argument est correct
     */
    public static boolean isValidTypeFormat(String type) {
        return type != null && type.length() == TYPE_LENGTH && isUpperCase(type);
    }

    /**
     * Vérifie que le format du pseudo passé en argument soit correct.
     *
     * @param id    pseudo
     * @return      true si le format du pseudo passé en argument est correct
     */
    public static boolean isValidIdFormat(String id, int minLength, int maxLength) {
        // TODO: gérer les caractères interdits pour un pseudo (ex: $![)*# ...)
        // => isAlphaNum()
        return id != null && id.length()>= minLength && id.length() <= maxLength;
    }

    /**
     * Vérifie que le format du contenu du message passé en argument soit correct.
     *
     * @param messageContent    contenu du message à envoyer
     * @return                  true si le format du contenu du message passé en argument est correct
     */
    public static boolean isValidMessageContentFormat(byte[] messageContent) {
        return messageContent != null && messageContent.length <= MESSAGE_CONTENT_SIZE;
    }

    /**
     * Vérifie si la string passée en argument est composée uniquement de majuscule.
     *
     * @param s
     * @return true si la string passée en argument est composée uniquement de majuscule
     */
    public static boolean isUpperCase(String s) {
        if(s == null | s == "") {
            return false;
        }
        for(int i = 0; i < s.length(); ++i) {
            if(!Character.isUpperCase(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ajoute n zéros devant l'entier pour correspondre au format souhaité.
     *
     * @param  number           entier à padder
     * @param  numberOfDigits   taille du padding
     * @return String paddé avec le nombre souhaité de zéros
     */
    public static String formatInt(int number, int numberOfDigits) {
        // TODO: trouver un meilleur nom de méthode
        // TODO: créer une classe utils avec ce genre de méthode ?
        StringBuilder paddingRule = new StringBuilder();
        paddingRule.append("%0").append(numberOfDigits).append("d");

        return String.format(paddingRule.toString(), number);
    }

    /**
     * Ajoute du padding (si nécessaire) au texte.
     *
     * @param text          texte à "padder"
     * @param sizeWithPad   taille totale du texte après ajout du padding
     * @param padSymbol     Symbole ajouté lors du padding
     * @return              texte paddé
     */
    public static String addPadding(String text, int sizeWithPad, char padSymbol) {
        StringBuilder result = new StringBuilder(text);

        while(result.length() < sizeWithPad) {
            result.append(padSymbol);
        }

        return result.toString();
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

    /**
     * Retourne le message de format peer.PeerMessage et prêt à l'envoi
     *
     * @return peer.PeerMessage formatté
     */
    public byte[] getFormattedMessage() {
        StringBuilder message = new StringBuilder();

        message.append(type).append(",");
        message.append(addPadding(idGroup, ID_GROUP_MAX_LENGTH, PADDING_SYMBOL)).append(",");
        message.append(addPadding(idFrom, ID_MAX_LENGTH, PADDING_SYMBOL)).append(",");
        message.append(addPadding(idTo, ID_MAX_LENGTH, PADDING_SYMBOL)).append(",");
        message.append(formatInt(noPacket, NO_PACKET_DIGITS)).append(",");

        // -1 sinon bug
        message.append(addPadding(new String(messageContent), MESSAGE_CONTENT_SIZE-1, PADDING_SYMBOL));

        return message.toString().getBytes();
    }

}
