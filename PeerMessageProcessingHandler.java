import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Date;
import java.util.Set;

/**
 * Manages reading and writing messages from/to a socket.
 */
public class PeerMessageProcessingHandler implements Runnable {

    
    private static String present_PID_dav;
    
    private RandomAccessFile rdm_acs_dav;

    /**
     * Constructor to initialize PeerMessageProcessingHandler object with peerID from arguments
     *
     * @param PID_dav 
     */
    public PeerMessageProcessingHandler(String PID_dav) {
        present_PID_dav = PID_dav;
    }

    
    public PeerMessageProcessingHandler() {
        present_PID_dav = null;
    }

    /**
     * Runs whenever the PeerMessageHandler thread is started.
     * Supports two types of connections:
     * - Active Connection: Performs initial handshake and sends bitfield messages to the socket.
     * - Passive Connection: Reads messages from the socket and adds them to the message queue.
     */
    @Override
    public void run() {
        MessageDetails msg_details_dav;
        Message message;
        String msg_ty_dav;
        String remotePeerID;

        while (true) {
            //Read message from queue
            msg_details_dav = MessageQueue.getmsgFrmQueueDav();
            while (msg_details_dav == null) {
                Thread.currentThread();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                msg_details_dav = MessageQueue.getmsgFrmQueueDav();
            }
            message = msg_details_dav.getMessage();
            msg_ty_dav = message.getType();
            remotePeerID = msg_details_dav.getFromPeerID();
            int peerState = peerProcess.remotePeerDetailsMap.get(remotePeerID).getPeerState();

            if (msg_ty_dav.equals(MessageConstants.MESSAGE_HAVE) && peerState != 14) {
             
                logAndShowInConsole(present_PID_dav + " contains interesting pieces from Peer " + remotePeerID);
                if (isPeerInterested(message, remotePeerID)) {
                    sendInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                    peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(9);
                } else {
                    sendNotInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                    peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                }
            } else {
                if (peerState == 2) {
                    if (msg_ty_dav.equals(MessageConstants.MESSAGE_BITFIELD)) {
                        
                        logAndShowInConsole(present_PID_dav + " received a BITFIELD message from Peer " + remotePeerID);
                        sendBitFieldMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(3);
                    }
                } else if (peerState == 3) {
                    if (msg_ty_dav.equals(MessageConstants.MESSAGE_INTERESTED)) {
                        
                        logAndShowInConsole(present_PID_dav + " receieved an INTERESTED message from Peer " + remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsInterested(1);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsHandShaked(1);
                        
                        if (isNotPreferredAndUnchokedNeighbour(remotePeerID)) {
                            sendChokedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(1);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(6);
                        } else {
                            sendUnChokedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(0);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(4);
                        }
                    } else if (msg_ty_dav.equals(MessageConstants.MESSAGE_NOT_INTERESTED)) {
                        
                        logAndShowInConsole(present_PID_dav + " receieved an NOT INTERESTED message from Peer " + remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsInterested(0);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsHandShaked(1);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(5);
                    }
                } else if (peerState == 4) {
                    if (msg_ty_dav.equals(MessageConstants.MESSAGE_REQUEST)) {
                       
                        sendFilePiece(peerProcess.peerToSocketMap.get(remotePeerID), message, remotePeerID);

                        Set<String> remotePeerDetailsKeys = peerProcess.remotePeerDetailsMap.keySet();
                        if (!peerProcess.isFirstPeer && peerProcess.bitFieldMessage.isFileDownloadComplete()) {
                            for (String key : remotePeerDetailsKeys) {
                                if (!key.equals(peerProcess.currentPeerID)) {
                                    Socket socket = peerProcess.peerToSocketMap.get(key);
                                    if (socket != null) {
                                        sendDownloadCompleteMessage(socket, key);
                                    }
                                }
                            }
                        }
                        if (isNotPreferredAndUnchokedNeighbour(remotePeerID)) {
                            
                            sendChokedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(1);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(6);
                        }
                    }
                } else if (peerState == 8) {
                    if (msg_ty_dav.equals(MessageConstants.MESSAGE_BITFIELD)) {
                        
                        if (isPeerInterested(message, remotePeerID)) {
                            sendInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(9);
                        } else {
                            sendNotInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                        }
                    }
                } else if (peerState == 9) {
                    if (msg_ty_dav.equals(MessageConstants.MESSAGE_CHOKE)) {
                        //Received choke message
                        logAndShowInConsole(present_PID_dav + " is CHOKED by Peer " + remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(1);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(14);
                    } else if (msg_ty_dav.equals(MessageConstants.MESSAGE_UNCHOKE)) {
                        //Received unchoke message
                        logAndShowInConsole(present_PID_dav + " is UNCHOKED by Peer " + remotePeerID);
                        //get the piece index which is present in remote peer but not in current peer and send a request message
                        int firstDifferentPieceIndex = getFirstDifferentPieceIndex(remotePeerID);
                        if (firstDifferentPieceIndex == -1) {
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                        } else {
                            sendRequestMessage(peerProcess.peerToSocketMap.get(remotePeerID), firstDifferentPieceIndex, remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(11);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setStartTime(new Date());
                        }
                    }
                } else if (peerState == 11) {
                    if (msg_ty_dav.equals(MessageConstants.MESSAGE_CHOKE)) {
                        //Received choke message
                        logAndShowInConsole(present_PID_dav + " is CHOKED by Peer " + remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(1);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(14);
                    } else if (msg_ty_dav.equals(MessageConstants.MESSAGE_PIECE)) {
                        //Received piece message
                        byte[] payloadInBytes = message.getPayload();
                        //compute data downloading rate of the peer
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setEndTime(new Date());
                        long totalTime = peerProcess.remotePeerDetailsMap.get(remotePeerID).getEndTime().getTime()
                                - peerProcess.remotePeerDetailsMap.get(remotePeerID).getStartTime().getTime();
                        double dataRate = ((double) (payloadInBytes.length + MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE) / (double) totalTime) * 100;
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setDataRate(dataRate);
                        FilePiece filePiece = FilePiece.convertByteArrayToFilePiece(payloadInBytes);
                        //update the piece information in current peer bitfield
                        peerProcess.bitFieldMessage.updateBitFieldInformation(remotePeerID, filePiece);
                        int firstDifferentPieceIndex = getFirstDifferentPieceIndex(remotePeerID);
                        if (firstDifferentPieceIndex == -1) {
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                        } else {
                            sendRequestMessage(peerProcess.peerToSocketMap.get(remotePeerID), firstDifferentPieceIndex, remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(11);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setStartTime(new Date());
                        }

                        peerProcess.updateOtherPeerDetails();
                        Set<String> remotePeerDetailsKeys = peerProcess.remotePeerDetailsMap.keySet();
                        for (String key : remotePeerDetailsKeys) {
                            RemotePeerDetails peerDetails = peerProcess.remotePeerDetailsMap.get(key);
                           
                            if (!key.equals(peerProcess.currentPeerID) && hasPeerInterested(peerDetails)) {
                                sendHaveMessage(peerProcess.peerToSocketMap.get(key), key);
                                peerProcess.remotePeerDetailsMap.get(key).setPeerState(3);
                            }
                        }

                        payloadInBytes = null;
                        message = null;
                        if (!peerProcess.isFirstPeer && peerProcess.bitFieldMessage.isFileDownloadComplete()) {
                            for (String key : remotePeerDetailsKeys) {
                                RemotePeerDetails peerDetails = peerProcess.remotePeerDetailsMap.get(key);
                                if (!key.equals(peerProcess.currentPeerID)) {
                                    Socket socket = peerProcess.peerToSocketMap.get(key);
                                    if (socket != null) {
                                        sendDownloadCompleteMessage(socket, key);
                                    }
                                }
                            }
                        }
                    }
                } else if (peerState == 14) {
                    if (msg_ty_dav.equals(MessageConstants.MESSAGE_HAVE)) {
                        //Received contains interesting pieces
                        logAndShowInConsole(present_PID_dav + " contains interesting pieces from Peer " + remotePeerID);
                        if (isPeerInterested(message, remotePeerID)) {
                            sendInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(9);
                        } else {
                            sendNotInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                        }
                    } else if (msg_ty_dav.equals(MessageConstants.MESSAGE_UNCHOKE)) {
                        //Received unchoked message
                        logAndShowInConsole(present_PID_dav + " is UNCHOKED by Peer " + remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(14);
                    }
                } else if (peerState == 15) {
                    try {
                        //update neighbor details after it gets file completely
                        peerProcess.remotePeerDetailsMap.get(peerProcess.currentPeerID).updatePeerDetails(remotePeerID, 1);
                        logAndShowInConsole(remotePeerID + " has downloaded the complete file");
                        int previousState = peerProcess.remotePeerDetailsMap.get(remotePeerID).getPreviousPeerState();
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(previousState);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * send DOWNLOAD COMPLETE message to socket
     *
     * @param socket 
     * @param PID_dav 
     */
    private void sendDownloadCompleteMessage(Socket socket, String PID_dav) {
        logAndShowInConsole(present_PID_dav + " sending a DOWNLOAD COMPLETE message to Peer " + PID_dav);
        Message message = new Message(MessageConstants.MESSAGE_DOWNLOADED);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    /**
     *  send HAVE message to socket
     *
     * @param socket 
     * @param PID_dav 
     */
    private void sendHaveMessage(Socket socket, String PID_dav) {
        
        byte[] bitFieldInBytes = peerProcess.bitFieldMessage.getBytes();
        Message message = new Message(MessageConstants.MESSAGE_HAVE, bitFieldInBytes);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(message));

        bitFieldInBytes = null;
    }

    /**
     * check remote peer is interested to receive messages
     *
     * @param remotePeerDetails
     * @return true 
     */
    private boolean hasPeerInterested(RemotePeerDetails remotePeerDetails) {
        return remotePeerDetails.getIsComplete() == 0 &&
                remotePeerDetails.getIsChoked() == 0 && remotePeerDetails.getIsInterested() == 1;
    }

    /**
     *  get the index of first piece different from current piece
     *
     * @param PID_dav 
     * @return index of the first different piece
     */
    private int getFirstDifferentPieceIndex(String PID_dav) {
        return peerProcess.bitFieldMessage.getFirstDifferentPieceIndex(peerProcess.remotePeerDetailsMap.get(PID_dav).getBitFieldMessage());
    }

    /**
     * send REQUEST message to socket
     *
     * @param socket       
     * @param pieceIndex   
     * @param remotePeerID 
     */
    private void sendRequestMessage(Socket socket, int pieceIndex, String remotePeerID) {
        logAndShowInConsole(peerProcess.currentPeerID + " sending REQUEST message to Peer " + remotePeerID + " for piece " + pieceIndex);
        int pieceIndexLength = MessageConstants.PIECE_INDEX_LENGTH;
        byte[] pieceInBytes = new byte[pieceIndexLength];
        for (int i = 0; i < pieceIndexLength; i++) {
            pieceInBytes[i] = 0;
        }

        byte[] pieceIndexInBytes = PeerProcessUtils.convertIntToByteArray(pieceIndex);
        System.arraycopy(pieceIndexInBytes, 0, pieceInBytes, 0, pieceIndexInBytes.length);
        Message message = new Message(MessageConstants.MESSAGE_REQUEST, pieceIndexInBytes);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(message));

        pieceInBytes = null;
        pieceIndexInBytes = null;
        message = null;
    }

    /**
     * This method is used to send File piece to socket
     *
     * @param socket       - socket in which the message to be sent
     * @param message      - message to be sent
     * @param remotePeerID - peerID to which the message should be sent
     */
    private void sendFilePiece(Socket socket, Message message, String remotePeerID) {
        byte[] pieceIndexInBytes = message.getPayload();
        int pieceIndex = PeerProcessUtils.convertByteArrayToInt(pieceIndexInBytes);
        int pieceSize = CommonConfiguration.pieceSize;
        logAndShowInConsole(present_PID_dav + " sending a PIECE message for piece " + pieceIndex + " to peer " + remotePeerID);

        byte[] bytesRead = new byte[pieceSize];
        int numberOfBytesRead = 0;
        File file = new File(present_PID_dav, CommonConfiguration.fileName);
        try {
            rdm_acs_dav = new RandomAccessFile(file, "r");
            rdm_acs_dav.seek(pieceIndex * pieceSize);
            numberOfBytesRead = rdm_acs_dav.read(bytesRead, 0, pieceSize);

            byte[] buffer = new byte[numberOfBytesRead + MessageConstants.PIECE_INDEX_LENGTH];
            System.arraycopy(pieceIndexInBytes, 0, buffer, 0, MessageConstants.PIECE_INDEX_LENGTH);
            System.arraycopy(bytesRead, 0, buffer, MessageConstants.PIECE_INDEX_LENGTH, numberOfBytesRead);

            Message messageToBeSent = new Message(MessageConstants.MESSAGE_PIECE, buffer);
            SendMessageToSocket(socket, Message.convertMessageToByteArray(messageToBeSent));
            rdm_acs_dav.close();

            buffer = null;
            bytesRead = null;
            pieceIndexInBytes = null;
            messageToBeSent = null;
        } catch (IOException e) {

        }
    }

    /**
     * This method is used if remote peer is not a preferred neighbor or optimistically unchoked neighbor.
     *
     * @param remotePeerId 
     * @return true 
     * false - remote peer is preferred neighbor or optimistically unchoked neighbor
     */
    private boolean isNotPreferredAndUnchokedNeighbour(String remotePeerId) {
        return !peerProcess.preferredNeighboursMap.containsKey(remotePeerId) && !peerProcess.optimisticUnchokedNeighbors.containsKey(remotePeerId);
    }

    /**
     * send CHOKE message to socket
     *
     * @param socket       
     * @param remotePeerID 
     */
    private void sendChokedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(present_PID_dav + " sending a CHOKE message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_CHOKE);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    /**
     *  send UNCHOKE message to socket
     *
     * @param socket       
     * @param remotePeerID 
     */
    private void sendUnChokedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(present_PID_dav + " sending a UNCHOKE message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_UNCHOKE);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    /**
     * send NOT INTERESTED message to socket
     *
     * @param socket       
     * @param remotePeerID 
     */
    private void sendNotInterestedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(present_PID_dav + " sending a NOT INTERESTED message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_NOT_INTERESTED);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    /**
     * send INTERESTED message to socket
     *
     * @param socket       
     * @param remotePeerID 
     */
    private void sendInterestedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(present_PID_dav + " sending an INTERESTED message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_INTERESTED);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    /**
     * send BITFIELD message to socket
     *
     * @param socket       
     * @param remotePeerID 
     */
    private void sendBitFieldMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(present_PID_dav + " sending a BITFIELD message to Peer " + remotePeerID);
        byte[] bitFieldMessageInByteArray = peerProcess.bitFieldMessage.getBytes();
        Message message = new Message(MessageConstants.MESSAGE_BITFIELD, bitFieldMessageInByteArray);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);

        bitFieldMessageInByteArray = null;
    }

    /**
     *  check if a peer is interested to receive messages.
     *
     * @param message      
     * @param remotePeerID 
     * @return true 
     */
    private boolean isPeerInterested(Message message, String remotePeerID) {
        boolean peerInterested = false;
        BitFieldMessage bitField = BitFieldMessage.decodeMessage(message.getPayload());
        peerProcess.remotePeerDetailsMap.get(remotePeerID).setBitFieldMessage(bitField);
        int pieceIndex = peerProcess.bitFieldMessage.getInterestingPieceIndex(bitField);
        if (pieceIndex != -1) {
            if (message.getType().equals(MessageConstants.MESSAGE_HAVE))
                logAndShowInConsole(present_PID_dav + " received HAVE message from Peer " + remotePeerID + " for piece " + pieceIndex);
            peerInterested = true;
        }

        return peerInterested;
    }

    /**
     * write a message to socket
     *
     * @param socket        
     * @param messageInBytes 
     */
    private void SendMessageToSocket(Socket socket, byte[] messageInBytes) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(messageInBytes);
        } catch (IOException e) {
        }
    }

    /**
     * log a message in a log file and show it in console
     *
     * @param message
     */
    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
