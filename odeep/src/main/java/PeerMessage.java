/**
 * Header Format:
 *
 * TYPE,idFROM==========,idTo============,noPaquet,MessageContentOn4048bytes
 * TYPE         4 lettres maj
 * idFROM       Source, 16 chars max
 * idTO         Dest,   16 chars max
 * noPaquet     numéro du paquet, int 8 digits (max 400Go)
 *
 * bytes total header  = 4+1+16+1+16+1+8+1 = 48 bytes
 * bytes total message = 4096 - 48 = 4048 bytes
 *
 */
public class PeerMessage {

    private static final int TYPE_LENGTH          =      4;
    private static final int ID_MAX_LENGTH        =     16;
    private static final int ID_MIN_LENGTH        =      6;
    private static final int NO_PACKET_DIGITS     =      8;
    private static final int BLOCK_SIZE           =   4096;
    private static final int HEADER_SIZE          = TYPE_LENGTH + 2 * ID_MAX_LENGTH + NO_PACKET_DIGITS + 4;
    private static final int MESSAGE_CONTENT_SIZE = BLOCK_SIZE - HEADER_SIZE;

    /**
     * EN-TÊTE DU MESSAGE
     */
    private String type;            // type du message
    private String idFrom;          // pseudo source
    private String idTo;            // pseudo destinataire
    private int    noPacket;        // numéro du paquet

    /**
     * CONTENU DU MESSAGE
     */
    private byte[] messageContent;


    public PeerMessage(String type, String idFrom, String idTo, int noPacket, byte[] messageContent) throws IllegalArgumentException{

        if(!isValidTypeFormat(type)) {
            throw new IllegalArgumentException("Bad 'type' format");
        }

        if(!isValidIdFormat(idFrom)) {
            throw new IllegalArgumentException("Bad 'idFrom' format");
        }

        if(!isValidIdFormat(idTo)) {
            throw new IllegalArgumentException("Bad 'idTo' format");
        }

        if(noPacket < 0) {
            throw new IllegalArgumentException("Invalid packet number (must be positiv)");
        }

        if(!isValidMessageContentFormat(messageContent)) {
            throw new IllegalArgumentException("Invalid message content (size must match block size (" + MESSAGE_CONTENT_SIZE + " bytes))");
        }

        this.type = type;
        this.idFrom = idFrom;
        this.idTo = idTo;
        this.noPacket = noPacket;
        this.messageContent = messageContent;
    }

    public PeerMessage(String type, String idFrom, String idTo, byte[] message) {
        this(type, idFrom, idTo, 0, message);
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
    public static boolean isValidIdFormat(String id) {
        // TODO: gérer les caractères interdits pour un pseudo (ex: $![)*# ...)
        return id != null && id.length()>= ID_MIN_LENGTH && id.length() <= ID_MAX_LENGTH;
    }

    /**
     * Vérifie que le format du contenu du message passé en argument soit correct.
     *
     * @param messageContent    contenu du message à envoyer
     * @return                  true si le format du contenu du message passé en argument est correct
     */
    public static boolean isValidMessageContentFormat(byte[] messageContent) {
        return messageContent != null && messageContent.length == MESSAGE_CONTENT_SIZE;
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

    public String getType() {
        return type;
    }

    public String getIdFrom() {
        return idFrom;
    }

    public String getIdTo() {
        return idTo;
    }

    public byte[] getMessageContent() {
        return messageContent;
    }

    /**
     * Retourne le message de format PeerMessage et prêt à l'envoi
     *
     * @return PeerMessage formatté
     */
    public byte[] getFormattedMessage() {
        StringBuilder message = new StringBuilder();

        message.append(type).append(",").append(idFrom).append(",").append(idTo).append(",").append(formatInt(noPacket, NO_PACKET_DIGITS)).append(",").append(new String(messageContent));

        return message.toString().getBytes();
    }

}
