/**
 * Manages message details and associated metadata.
 */
public class MessageDetails {
    
    private Message msg_dav;
    // Sender's peerID
    private String from_PID_dav;

     /**
     * Constructs a MessageDetails instance and initializes required fields.
     */
    public MessageDetails() {
        msg_dav = new Message();
        from_PID_dav = null;
    }

     /**
     * Retrieves the message.
     * @return msg_dav
     */
    public Message getMessage() {
        return msg_dav;
    }

    /**
     * Sets the message.
     * @param msg_dav
     */
    public void setMessage(Message msg_dav) {
        this.msg_dav = msg_dav;
    }

   /**
     * Retrieves the peerID of the sender.
     * @return peerID
     */
    public String getFromPeerID() {
        return from_PID_dav;
    }

    /**
     * Sets the peerID of the sender.
     * @param from_PID_dav
     */
    public void setFromPeerID(String from_PID_dav) {
        this.from_PID_dav = from_PID_dav;
    }

}