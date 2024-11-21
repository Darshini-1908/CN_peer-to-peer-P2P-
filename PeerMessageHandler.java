import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Manages reading and writing messages from/to a socket.
 */
public class PeerMessageHandler implements Runnable {
    
    private Socket peer_skt_dav = null;
    
    private int con_ty_dav;
    
    String own_PID_dav;
   
    String remote_PID_dav;
    
    private InputStream skt_in_dav;
    
    private OutputStream skt_out_dav;
   
    private HandshakeMessage hand_shk_msg_dav;

     /**
     * Initializes the PeerMessageHandler object and sets up the required fields.
     * @param dav_add 
     * @param dav_port 
     * @param dav_conn_Ty 
     * @param dav_server_PID 
     */
    public PeerMessageHandler(String dav_add, int dav_port, int dav_conn_Ty, String dav_server_PID) {
        try {
            con_ty_dav = dav_conn_Ty;
            own_PID_dav = dav_server_PID;
            peer_skt_dav = new Socket(dav_add, dav_port);
            skt_in_dav = peer_skt_dav.getInputStream();
            skt_out_dav = peer_skt_dav.getOutputStream();
        } catch (IOException e) {
        }
    }

    /**
     * Initializes the PeerMessageHandler object and sets up the required fields.
     * @param socket 
     * @param dav_conn_Ty 
     * @param dav_server_PID 
     */
    public PeerMessageHandler(Socket socket, int dav_conn_Ty, String dav_server_PID) {
        try {
            peer_skt_dav = socket;
            con_ty_dav = dav_conn_Ty;
            own_PID_dav = dav_server_PID;
            skt_in_dav = peer_skt_dav.getInputStream();
            skt_out_dav = peer_skt_dav.getOutputStream();
        } catch (IOException e) {

        }
    }
/**
     * Retrieves the socket instance.
     * @return Socket instance
     */
    public Socket getPeerSocket() {
        return peer_skt_dav;
    }

     /**
     * Sets the socket instance.
     * @param peer_skt_dav 
     */
    public void setPeerSocket(Socket peer_skt_dav) {
        this.peer_skt_dav = peer_skt_dav;
    }

     /**
     * Retrieves the type of connection established.
     * @return type of connection established
     */
    public int getConnType() {
        return con_ty_dav;
    }

    /**
     * Sets the type of connection established.
     * @param con_ty_dav - connection type to be set
     */
    public void setConnType(int con_ty_dav) {
        this.con_ty_dav = con_ty_dav;
    }

    /**
     * sets the current host peerID
     * @return current host peerID
     */
    public String getOwnPeerId() {
        return own_PID_dav;
    }

    /**
     * sets the current host peerID
     * @param own_PID_dav - current host peerID
     */
    public void setOwnPeerId(String own_PID_dav) {
        this.own_PID_dav = own_PID_dav;
    }

    /**
     * Get the remote host peerID
     * @return remote host peerID
     */
    public String getRemotePeerId() {
        return remote_PID_dav;
    }

    /**
     * sets the remote host peerID
     * @param remote_PID_dav - remote host peerID
     */
    public void setRemotePeerId(String remote_PID_dav) {
        this.remote_PID_dav = remote_PID_dav;
    }

    /**
     * This method is used to get the socket input stream
     * @return socket input stream
     */
    public InputStream getSocketInputStream() {
        return skt_in_dav;
    }

    /**
     * This method is used to set the socket input stream
     * @param skt_in_dav - socket input stream
     */
    public void setSocketInputStream(InputStream skt_in_dav) {
        this.skt_in_dav = skt_in_dav;
    }

    /**
     * This method is used to get the socket output stream
     * @return socket output stream
     */
    public OutputStream getSocketOutputStream() {
        return skt_out_dav;
    }

    /**
     * This method is used to set the socket output stream
     * @param skt_out_dav - socket output stream
     */
    public void setSocketOutputStream(OutputStream skt_out_dav) {
        this.skt_out_dav = skt_out_dav;
    }

    /**
     * This method is used to get the handshake message
     * @return handshake message
     */
    public HandshakeMessage getHandshakeMessage() {
        return hand_shk_msg_dav;
    }

    /**
     * This method is used to set the handshake message
     * @param hand_shk_msg_dav - handshake message
     */
    public void setHandshakeMessage(HandshakeMessage hand_shk_msg_dav) {
        this.hand_shk_msg_dav = hand_shk_msg_dav;
    }

   /**
     * Runs whenever the PeerMessageHandler thread is started.
     * Supports two types of connections:
     * - Active Connection: Performs initial handshake and sends bitfield messages to the socket.
     * - Passive Connection: Reads messages from the socket and adds them to the message queue.
     */
    @Override
    public void run() {
        // Implementation of the run method...
        byte[] handShakeMessageInBytes = new byte[32];
        byte[] dataBufferWithoutPayload = new byte[MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE];
        byte[] messageLengthInBytes;
        byte[] messageTypeInBytes;
        MessageDetails messageDetails = new MessageDetails();
        try {
            //Initial connection of file receivers. Sending handshake and bitfield message
            if (con_ty_dav == MessageConstants.ACTIVE_CONNECTION) {

                if (handShakeMessageSent()) {
                    logAndShowInConsole(own_PID_dav + " HANDSHAKE has been sent");
                } else {
                    logAndShowInConsole(own_PID_dav + " HANDSHAKE sending failed");
                    System.exit(0);
                }

                while (true) {
                    skt_in_dav.read(handShakeMessageInBytes);
                    hand_shk_msg_dav = HandshakeMessage.convertBytesToHandshakeMessage(handShakeMessageInBytes);
                    if (hand_shk_msg_dav.getHeader().equals(MessageConstants.HANDSHAKE_HEADER)) {
                        remote_PID_dav = hand_shk_msg_dav.getPeerID();
                        logAndShowInConsole(own_PID_dav + " makes a connection to Peer " + remote_PID_dav);
                        logAndShowInConsole(own_PID_dav + " Received a HANDSHAKE message from Peer " + remote_PID_dav);
                        //populate peerID to socket mapping
                        peerProcess.peerToSocketMap.put(remote_PID_dav, this.peer_skt_dav);
                        break;
                    }
                }

                // Sending BitField...
                Message d = new Message(MessageConstants.MESSAGE_BITFIELD, peerProcess.bitFieldMessage.getBytes());
                byte[] b = Message.convertMessageToByteArray(d);
                skt_out_dav.write(b);
                peerProcess.remotePeerDetailsMap.get(remote_PID_dav).setPeerState(8);
            }

            //This type is used to send and receive messages and add received messages to message queue
            else {
                while (true) {
                    skt_in_dav.read(handShakeMessageInBytes);
                    hand_shk_msg_dav = HandshakeMessage.convertBytesToHandshakeMessage(handShakeMessageInBytes);
                    if (hand_shk_msg_dav.getHeader().equals(MessageConstants.HANDSHAKE_HEADER)) {
                        remote_PID_dav = hand_shk_msg_dav.getPeerID();
                        logAndShowInConsole(own_PID_dav + " is connected from Peer " + remote_PID_dav);
                        logAndShowInConsole(own_PID_dav + " Received a HANDSHAKE message from Peer " + remote_PID_dav);

                        //populate peerID to socket mapping
                        peerProcess.peerToSocketMap.put(remote_PID_dav, this.peer_skt_dav);
                        break;
                    } else {
                        continue;
                    }
                }
                if (handShakeMessageSent()) {
                    logAndShowInConsole(own_PID_dav + " HANDSHAKE message has been sent successfully.");

                } else {
                    logAndShowInConsole(own_PID_dav + " HANDSHAKE message sending failed.");
                    System.exit(0);
                }

                peerProcess.remotePeerDetailsMap.get(remote_PID_dav).setPeerState(2);
            }

            while (true) {
                int headerBytes = skt_in_dav.read(dataBufferWithoutPayload);
                if (headerBytes == -1)
                    break;
                messageLengthInBytes = new byte[MessageConstants.MESSAGE_LENGTH];
                messageTypeInBytes = new byte[MessageConstants.MESSAGE_TYPE];
                System.arraycopy(dataBufferWithoutPayload, 0, messageLengthInBytes, 0, MessageConstants.MESSAGE_LENGTH);
                System.arraycopy(dataBufferWithoutPayload, MessageConstants.MESSAGE_LENGTH, messageTypeInBytes, 0, MessageConstants.MESSAGE_TYPE);
                Message message = new Message();
                message.setMessageLength(messageLengthInBytes);
                message.setMessageType(messageTypeInBytes);
                String messageType = message.getType();
                if (messageType.equals(MessageConstants.MESSAGE_INTERESTED) || messageType.equals(MessageConstants.MESSAGE_NOT_INTERESTED) ||
                        messageType.equals(MessageConstants.MESSAGE_CHOKE) || messageType.equals(MessageConstants.MESSAGE_UNCHOKE)) {
                    messageDetails.setMessage(message);
                    messageDetails.setFromPeerID(remote_PID_dav);
                    MessageQueue.addMessageToMessageQueue(messageDetails);
                } else if (messageType.equals(MessageConstants.MESSAGE_DOWNLOADED)) {
                    messageDetails.setMessage(message);
                    messageDetails.setFromPeerID(remote_PID_dav);
                    int peerState = peerProcess.remotePeerDetailsMap.get(remote_PID_dav).getPeerState();
                    peerProcess.remotePeerDetailsMap.get(remote_PID_dav).setPreviousPeerState(peerState);
                    peerProcess.remotePeerDetailsMap.get(remote_PID_dav).setPeerState(15);
                    MessageQueue.addMessageToMessageQueue(messageDetails);
                } else {
                    int bytesAlreadyRead = 0;
                    int bytesRead;
                    byte[] dataBuffPayload = new byte[message.getMessageLengthAsInteger() - 1];
                    while (bytesAlreadyRead < message.getMessageLengthAsInteger() - 1) {
                        bytesRead = skt_in_dav.read(dataBuffPayload, bytesAlreadyRead, message.getMessageLengthAsInteger() - 1 - bytesAlreadyRead);
                        if (bytesRead == -1)
                            return;
                        bytesAlreadyRead += bytesRead;
                    }

                    byte[] dataBuffWithPayload = new byte[message.getMessageLengthAsInteger() + MessageConstants.MESSAGE_LENGTH];
                    System.arraycopy(dataBufferWithoutPayload, 0, dataBuffWithPayload, 0, MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE);
                    System.arraycopy(dataBuffPayload, 0, dataBuffWithPayload, MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE, dataBuffPayload.length);

                    Message dataMsgWithPayload = Message.convertByteArrayToMessage(dataBuffWithPayload);
                    messageDetails.setMessage(dataMsgWithPayload);
                    messageDetails.setFromPeerID(remote_PID_dav);
                    MessageQueue.addMessageToMessageQueue(messageDetails);
                    dataBuffWithPayload = null;
                    dataBuffPayload = null;
                    bytesAlreadyRead = 0;
                    bytesRead = 0;
                }
            }

        } catch (Exception e) {
        }
    }

     /**
     * Sends a handshake message to the socket and determines if the message is sent successfully.
     * @return true - Message sent successfully; false - Message not sent successfully
     */
    public boolean handShakeMessageSent() {
        boolean messageSent = false;
        try {
            HandshakeMessage hand_shk_msg_dav = new HandshakeMessage(MessageConstants.HANDSHAKE_HEADER, this.own_PID_dav);
            skt_out_dav.write(HandshakeMessage.convertHandshakeMessageToBytes(hand_shk_msg_dav));
            messageSent = true;
        } catch (IOException e) {
        }
        return messageSent;
    }

   /**
     * Logs a message in a log file and shows it in the console.
     * @param message 
     */
    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
