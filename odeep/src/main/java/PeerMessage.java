
public class PeerMessage {

    private static final int TYPE_LENGTH = 4;
    private String type;
    private String message;

    public PeerMessage(String type, String message) {
        //exception si pas bon format de type
        if(!isValidTypeFormat(type)) {
            throw new IllegalArgumentException("Bad type format");
        }
        this.type = type;
        this.message = message;
    }

    /**
     * Vérifie que le format du type de message passé en argument est correct.
     * @param type
     * @return
     */
    public static boolean isValidTypeFormat(String type) {
        return type != null && type.length() == TYPE_LENGTH && isUpperCase(type);
    }

    /**
     * Vérifie si la string passée en argument est composée uniquement de majuscule.
     * @param s
     * @return
     */
    private static boolean isUpperCase(String s) {
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

    public String getMessage() {
        return message;
    }

}
