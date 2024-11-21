import java.io.UnsupportedEncodingException;

/**
 * Represents the structure and handling of a handshake message.
 * It contains details such as the handshake header, peer ID, and zero bits padding.
 */
public class HandshakeMessage {
    //Handshake header in bytes
    private byte[] H_inbytes_J = new byte[MessageConstants.HANDSHAKE_HEADER_LENGTH];
    //Peer ID from where the handshake message is sent in bytes
    private byte[] P_ID_inb_J = new byte[MessageConstants.HANDSHAKE_PEERID_LENGTH];
    //zero bits to be padded at end in bytes
    private byte[] zero_J = new byte[MessageConstants.HANDSHAKE_ZEROBITS_LENGTH];
    //Handshake header
    private String H_J;
    //The Peer ID from where the handshake message is sent
    private String p_ID_J;

    //Empty constructor to create a handshake message object.
     
    public HandshakeMessage() {
    }

    /**
     * Constructor to create a handshake message with the header and peer ID.
     * It sets H_inbytes_J, P_ID_inb_J, zero_J, H_J and p_ID_J fields
     * @param H_J 
     * @param p_ID_J 
     */
    public HandshakeMessage(String H_J, String p_ID_J) {
        try {
            // Set the provided header string and convert it to bytes using the default character set
            this.H_J = H_J;
            this.H_inbytes_J = H_J.getBytes(MessageConstants.DEFAULT_CHARSET);
            if (this.H_inbytes_J.length > MessageConstants.HANDSHAKE_HEADER_LENGTH)
                throw new Exception("Too large Handshake Header");
            // Set the provided peer ID string and convert it to bytes using the default character set
            this.p_ID_J = p_ID_J;
            this.P_ID_inb_J = p_ID_J.getBytes(MessageConstants.DEFAULT_CHARSET);
            if (this.P_ID_inb_J.length > MessageConstants.HANDSHAKE_PEERID_LENGTH)
                throw new Exception("Too large Handshake PeerID");
            // Initialize the 'zero' bytes to a constant default value
            this.zero_J = "0000000000".getBytes(MessageConstants.DEFAULT_CHARSET);
        } catch (Exception e) {
        
        }
    }

    /**
     * Converts a handshake message to a byte array.
     * @param handshakeMessage 
     * @return byte array 
     */
    public static byte[] convertHandshakeMessageToBytes(HandshakeMessage handshakeMessage) {
        // Byte array to hold the converted HandshakeMessage
        byte[] handshakeMessageInBytes = new byte[MessageConstants.HANDSHAKE_MESSAGE_LENGTH];
        try {
            // Checks and copies the header bytes to the handshakeMessageInBytes array
            if (handshakeMessage.getH_inbytes_J() == null ||
                    (handshakeMessage.getH_inbytes_J().length > MessageConstants.HANDSHAKE_HEADER_LENGTH || handshakeMessage.getH_inbytes_J().length == 0))
                throw new Exception("Invalid Handshake Message Header");
            else
                System.arraycopy(handshakeMessage.getH_inbytes_J(), 0,
                        handshakeMessageInBytes, 0, handshakeMessage.getH_inbytes_J().length);
            
            // Checks and copies the zero bits bytes to the handshakeMessageInBytes array
            if (handshakeMessage.getzero_J() == null ||
                    (handshakeMessage.getzero_J().length > MessageConstants.HANDSHAKE_ZEROBITS_LENGTH || handshakeMessage.getzero_J().length == 0))
                throw new Exception("Invalid Handshake Message Zero Bits");
            else
                System.arraycopy(handshakeMessage.getzero_J(), 0,
                        handshakeMessageInBytes, MessageConstants.HANDSHAKE_HEADER_LENGTH, MessageConstants.HANDSHAKE_ZEROBITS_LENGTH - 1);
            
            // Checks and copies the peer ID bytes to the handshakeMessageInBytes array
            if (handshakeMessage.getP_ID_inb_J() == null ||
                    (handshakeMessage.getP_ID_inb_J().length > MessageConstants.HANDSHAKE_PEERID_LENGTH || handshakeMessage.getP_ID_inb_J().length == 0))
                throw new Exception("Invalid Handshake Message Peer ID");
            else
                System.arraycopy(handshakeMessage.getP_ID_inb_J(), 0, handshakeMessageInBytes,
                        MessageConstants.HANDSHAKE_HEADER_LENGTH + MessageConstants.HANDSHAKE_ZEROBITS_LENGTH,
                        handshakeMessage.getP_ID_inb_J().length);
        } catch (Exception e) {
            handshakeMessageInBytes = null; // If an exception occurs, set the byte array to null

        }

        return handshakeMessageInBytes;
    }

    /**
     * Converts a byte array to a handshake message object.
     * @param handShakeMessage 
     * @return - Handshake message object
     */
    public static HandshakeMessage convertBytesToHandshakeMessage(byte[] handShakeMessage) {
        HandshakeMessage message = null;

        try {
            // Checks if the length of the provided byte array matches the expected length of a HandshakeMessage
            if (handShakeMessage.length != MessageConstants.HANDSHAKE_MESSAGE_LENGTH)
                throw new Exception("Length is invalid while Decoding the Handshake message");
            // Initializes a new HandshakeMessage object
            message = new HandshakeMessage();

            // Arrays to hold header and peer ID bytes
            byte[] messageHeader = new byte[MessageConstants.HANDSHAKE_HEADER_LENGTH];
            byte[] messagePeerID = new byte[MessageConstants.HANDSHAKE_PEERID_LENGTH];
            
            // Copies bytes from the given byte array to the messageHeader and messagePeerID arrays
            System.arraycopy(handShakeMessage, 0, messageHeader, 0,
                    MessageConstants.HANDSHAKE_HEADER_LENGTH);
            System.arraycopy(handShakeMessage, MessageConstants.HANDSHAKE_HEADER_LENGTH
                            + MessageConstants.HANDSHAKE_ZEROBITS_LENGTH, messagePeerID, 0,
                    MessageConstants.HANDSHAKE_PEERID_LENGTH);
            
            // Sets the extracted header and peer ID bytes into the HandshakeMessage object
            message.setHeaderFromBytes(messageHeader);
            message.setPeerIDFromBytes(messagePeerID);

        } catch (Exception e) {

        }
        return message;
    }

    /**
     * Sets the Peer ID from a byte array representation.
     * @param messagePeerID 
     */
    public void setPeerIDFromBytes(byte[] messagePeerID) {
        try {
            // Converts the byte array to a string and trims any leading/trailing whitespaces.
            p_ID_J = (new String(messagePeerID, MessageConstants.DEFAULT_CHARSET)).trim();
            P_ID_inb_J = messagePeerID;
        } catch (UnsupportedEncodingException e) {
            logAndShowInConsole(e.getMessage());
        }
    }

    /**
     * Sets the handshake header from a byte array representation.
     * @param messageHeader 
     */
    public void setHeaderFromBytes(byte[] messageHeader) {
        try {
            H_J = (new String(messageHeader, MessageConstants.DEFAULT_CHARSET)).trim();
            H_inbytes_J = messageHeader;
        } catch (UnsupportedEncodingException e) {
            logAndShowInConsole(e.getMessage());
        }
    }

    /**
     * Gets the byte array representation of the peer ID.
     * @return - H_inbytes_J
     */
    public byte[] getH_inbytes_J() {
        return H_inbytes_J;
    }

    /**
     * Sets the byte array representation of the peer ID.
     * @param H_inbytes_J
     */
    public void setH_inbytes_J(byte[] H_inbytes_J) {
        this.H_inbytes_J = H_inbytes_J;
    }

    /**
     * Gets the zero bits in the handshake message.
     * @return P_ID_inb_J
     */
    public byte[] getP_ID_inb_J() {
        return P_ID_inb_J;
    }

    /**
     * Sets the zero bits in the handshake message.
     * @param P_ID_inb_J
     */
    public void setP_ID_inb_J(byte[] P_ID_inb_J) {
        this.P_ID_inb_J = P_ID_inb_J;
    }

    /**
     *Get handshake zero bits
     * @return zero_J
     */
    public byte[] getzero_J() {
        return zero_J;
    }

    /**
     * Set handshake zero bits
     * @param zero_J
     */
    public void setzero_J(byte[] zero_J) {
        this.zero_J = zero_J;
    }

    /**
     *Get handshake header
     * @return H_J
     */
    public String getHeader() {
        return H_J;
    }

    /**
     * Set handshake header
     * @param H_J
     */
    public void setheader(String H_J) {
        this.H_J = H_J;
    }

    /**
     * Get handshake peerID
     * @return p_ID_J
     */
    public String getPeerID() {
        return p_ID_J;
    }

    /**
     *Set handshake peerID
     * @param p_ID_J
     */
    public void setp_ID_J(String p_ID_J) {
        this.p_ID_J = p_ID_J;
    }

    /**
     * Logs a message in a log file and displays it in the console.
     * @param message 
     */
    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
