/**
 * Header Format:
 *
 * TYPE,idFROM==========,idTo============,noPaquet,
 * TYPE         4 lettres maj
 * idFROM       Source, 16 chars max
 * idTO         Dest,   16 chars max
 * noPaquet     numéro du paquet, int 8 digits (max 400Go)
 *
 * bytes total header  = 4+1+16+1+16+1+8+1 = 48 bits
 * bytes total message = 4096 - 48 = 4048 bits
 *
 *
 */
public class PeerMessage {

    private static final int TYPE_LENGTH      =    4;
    private static final int ID_LENGTH        =   16;
    private static final int NO_PACKET_DIGITS =    8;
    private static final int BLOCK_SIZE       = 4096;
    private String type;
    private Byte[] message;

    public PeerMessage(String type, String idFrom, String idTo, int noPacket, Byte[] message) throws IllegalArgumentException{
        //exception si pas bon format de type
        if(!isValidTypeFormat(type)) {
            throw new IllegalArgumentException("Bad type format");
        }
        this.type = type;



        //this.message = new Byte[BLOCK_SIZE];


    }

    public PeerMessage(String type, String idFrom, String idTo, Byte[] message) {
        this(type, idFrom, idTo, 1, message);
    }

    /**
     * Vérifie que le format du type de message passé en argument est correct.
     * @param type
     * @return true si le format du type de message passé en argument est correct
     */
    public static boolean isValidTypeFormat(String type) {
        return type != null && type.length() == TYPE_LENGTH && isUpperCase(type);
    }

    /**
     * Vérifie si la string passée en argument est composée uniquement de majuscule.
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

    public String getType() {
        return type;
    }

    public Byte[] getMessage() {
        return message;
    }

}
