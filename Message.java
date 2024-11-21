import java.io.UnsupportedEncodingException;

/**
 * Handles messages excluding handshake, bitfield, and piece messages.
 */
public class Message {
    //Type of message
    private String ty_J;
    //Length of message
    private String Ln_J;
    //ength of data 
    private int data_Ln_J = MessageConstants.MESSAGE_TYPE;
    //Type of message in bytes
    private byte[] ty_inB_J = null;
    //Length of message in bytes
    private byte[] Ln_inB_J = null;
    //Content in the message
    private byte[] Pl_J = null;

    /**
     * To create message object, an empty constructor
     */
    public Message() {
    }

    /**
     * create message object based on message type
     * @param messageType - type of the message
     */
    public Message(String messageType) {
        try {
            if (messageType == MessageConstants.MESSAGE_INTERESTED || messageType == MessageConstants.MESSAGE_NOT_INTERESTED ||
                    messageType == MessageConstants.MESSAGE_CHOKE || messageType == MessageConstants.MESSAGE_UNCHOKE
                    || messageType == MessageConstants.MESSAGE_DOWNLOADED) {
                setMessageLength(1);
                setMessageType(messageType);
                this.Pl_J = null;
            } else {
                logAndShowInConsole("When initialzing Message constructor,Error Occurred");
                throw new Exception("Message Constructor - selecte constructor is Wrong");
            }
        } catch (Exception e) {
            logAndShowInConsole(e.getMessage());
        }
    }

    /**
     * create message object based on type and payload
     * @param messageType - type of the message
     * @param Pl_J - message payload
     */
    public Message(String messageType, byte[] Pl_J) {
        try {
            if (Pl_J != null) {
                // If payload is not null, calculate and set the message length based on payload size
                setMessageLength(Pl_J.length + 1);
                // Check if the calculated message length exceeds the defined limit
                if (Ln_inB_J.length > MessageConstants.MESSAGE_LENGTH) {
                    logAndShowInConsole("When initialzing Message constructor,Error Occurred");
                    throw new Exception("Message Constructor - Too large Message Length");
                }
                // Set the payload for the Message object
                setPl_J(Pl_J);
            } else {
                if (messageType == MessageConstants.MESSAGE_INTERESTED || messageType == MessageConstants.MESSAGE_NOT_INTERESTED
                        || messageType == MessageConstants.MESSAGE_CHOKE || messageType == MessageConstants.MESSAGE_UNCHOKE
                        || messageType == MessageConstants.MESSAGE_DOWNLOADED) {
                    setMessageLength(1);
                    this.Pl_J = null;
                } else {
                    logAndShowInConsole("When initialzing Message constructor,Error Occurred");
                    throw new Exception("Message Constructor - Message Payload should not be null");
                }
            }
            // Set the message type for the Message object
            setMessageType(messageType);
            if (ty_inB_J.length > MessageConstants.MESSAGE_TYPE) {
                logAndShowInConsole("When initialzing Message constructor,Error Occurred");
                throw new Exception("Message Constructor - Too large Message Type length");
            }
        } catch (Exception e) {
            logAndShowInConsole("When initialzing Message constructor,Error Occurred - " + e.getMessage());
        }
    }

    /**
     * This method is used to set message type and message type in bytes with message type received in params
     * @param messageType - type of message to be set
     */
    public void setMessageType(String messageType) {
        ty_J = messageType.trim();
        try {
            ty_inB_J = messageType.getBytes(MessageConstants.DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            logAndShowInConsole(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method is used to set message length, data length and message length in bytes with message length received in params
     * @param messageLength - length of message to be set
     */
    public void setMessageLength(int messageLength) {
        data_Ln_J = messageLength;
        Ln_J = ((Integer) messageLength).toString();
        Ln_inB_J = PeerProcessUtils.convertIntToByteArray(messageLength);
    }

    /**
     * This method is used to set message length, data length and message length in bytes with message length received in params
     * @param len - length of message to be set
     */
    public void setMessageLength(byte[] len) {

        Integer l = PeerProcessUtils.convertByteArrayToInt(len);
        this.Ln_J = l.toString();
        this.Ln_inB_J = len;
        this.data_Ln_J = l;
    }

    /**
     * This method is used to set message type and message type in bytes with message type received in params
     * @param ty_J - type of message to be set
     */
    public void setMessageType(byte[] ty_J) {
        try {
            this.ty_J = new String(ty_J, MessageConstants.DEFAULT_CHARSET);
            this.ty_inB_J = ty_J;
        } catch (UnsupportedEncodingException e) {
            logAndShowInConsole(e.toString());
        }
    }

    /**
     * This method is used to return message data length
     * @return message data length
     */
    public int getMessageLengthAsInteger() {
        return this.data_Ln_J;
    }

    /**
     * This method is used to convert message to byte array
     * @param message - Message instance to be converted
     * @return byte array of the message
     */
    public static byte[] convertMessageToByteArray(Message message) {
        byte[] messageInByteArray = null;
        try {
            int messageType = Integer.parseInt(message.getType());
            // Check if the length of message type or message length is invalid
            if (message.getLn_inB_J().length > MessageConstants.MESSAGE_LENGTH)
                throw new Exception("Invalid Message Length");
            else if (messageType < 0 || messageType > 8)
                throw new Exception("Invalid Message Type");
                // Check if the message type or length bytes are null
            else if (message.getty_inB_J() == null)
                throw new Exception("Invalid Message Type");
            else if (message.getLn_inB_J() == null)
                throw new Exception("Invalid Message Length");
            // Create the byte array representation based on message contents
            if (message.getPayload() != null) {
                messageInByteArray = new byte[MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE + message.getPayload().length];
                System.arraycopy(message.getLn_inB_J(), 0, messageInByteArray, 0, message.getLn_inB_J().length);
                System.arraycopy(message.getty_inB_J(), 0, messageInByteArray, MessageConstants.MESSAGE_LENGTH, MessageConstants.MESSAGE_TYPE);
                System.arraycopy(message.getPayload(), 0, messageInByteArray,
                        MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE, message.getPayload().length);
            } else {
                messageInByteArray = new byte[MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE];
                System.arraycopy(message.getLn_inB_J(), 0, messageInByteArray, 0, message.getLn_inB_J().length);
                System.arraycopy(message.getty_inB_J(), 0, messageInByteArray, MessageConstants.MESSAGE_LENGTH, MessageConstants.MESSAGE_TYPE);
            }
        } catch (Exception e) {
        }

        return messageInByteArray;
    }

    /**
     * This method is used to convert byte array into message object
     * @param message - byte array to be converted
     * @return message instance
     */
    public static Message convertByteArrayToMessage(byte[] message) {

        Message msg = new Message();
        byte[] msgLength = new byte[MessageConstants.MESSAGE_LENGTH];
        byte[] msgType = new byte[MessageConstants.MESSAGE_TYPE];
        byte[] Pl_J = null;
        int len;

        try {
            // Check for invalid or insufficient data in the byte array
            if (message == null)
                throw new Exception("Data is Invalid.");
            else if (message.length < MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE)
                throw new Exception("Too small Byte array length.");

            // Extract message length and message type bytes from the given array
            System.arraycopy(message, 0, msgLength, 0, MessageConstants.MESSAGE_LENGTH);
            System.arraycopy(message, MessageConstants.MESSAGE_LENGTH, msgType, 0, MessageConstants.MESSAGE_TYPE);
            // Set message length and type in the Message object
            msg.setMessageLength(msgLength);
            msg.setMessageType(msgType);

            // Convert the message length bytes to an integer
            len = PeerProcessUtils.convertByteArrayToInt(msgLength);
            
            // Extract and set payload if the length is greater than 1
            if (len > 1) {
                Pl_J = new byte[len - 1];
                System.arraycopy(message, MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE, Pl_J, 0, message.length - MessageConstants.MESSAGE_LENGTH - MessageConstants.MESSAGE_TYPE);
                msg.setPl_J(Pl_J);
            }

            Pl_J = null;
        } catch (Exception e) {
            LogHelper.logAndShowInConsole(e.toString());
            msg = null;
        }
        return msg;
    }

    /**
     * Gets the type of message
     * @return 
     */
    public String getType() {
        return ty_J;
    }

    /**
     * Sets the type of message
     * @param ty_J 
     */
    public void setType(String ty_J) {
        this.ty_J = ty_J;
    }

    /**
     * Gets the length of message
     * @return 
     */
    public String getLength() {
        return Ln_J;
    }

    /**
     * Sets the length of message
     * @param Ln_J 
     */
    public void setLength(String Ln_J) {
        this.Ln_J = Ln_J;
    }

    /**
     * Gets the type of message in bytes
     * @return 
     */
    public byte[] getty_inB_J() {
        return ty_inB_J;
    }

    /**
     * sets the type of message in bytes
     * @param ty_inB_J - 
     */
    public void setty_inB_J(byte[] ty_inB_J) {
        this.ty_inB_J = ty_inB_J;
    }

    /**
     * Gets the length of message in bytes
     * @return Ln_J of the message in bytes
     */
    public byte[] getLn_inB_J() {
        return Ln_inB_J;
    }

    /**
     * Sets the length of message in bytes
     * @param Ln_inB_J 
     */
    public void setLn_inB_J(byte[] Ln_inB_J) {
        this.Ln_inB_J = Ln_inB_J;
    }

    /**
     * Gets the content of message
     * @return 
     */
    public byte[] getPayload() {
        return Pl_J;
    }

    /**
     * Sets the content of message
     * @param Pl_J 
     */
    public void setPl_J(byte[] Pl_J) {
        this.Pl_J = Pl_J;
    }

    /**
     * Logs a message in a log file and displays it in console
     * @param message 
     */
    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }

}
