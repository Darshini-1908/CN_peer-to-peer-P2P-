import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Set;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Determining optimistically unchoked neighbor from choked neighbors
 */
public class OptimisticallyUnchockedNeighbors extends TimerTask {

    /**
     * This method runs asynchronously as part of timer task every 'CommonConfiguration.optimisticUnchokingInterval' interval.
     * Determines optimistically unchoked neighbor at random from all the neighbors choked.
     */
    @Override
    public void run() {
        peerProcess.updateOtherPeerDetails();
        if (!peerProcess.optimisticUnchokedNeighbors.isEmpty()) {
            peerProcess.optimisticUnchokedNeighbors.clear();
        }

        //Interested peers to a list
        Set<String> k_dav = peerProcess.remotePeerDetailsMap.keySet();
        Vector<RemotePeerDetails> detlsRemotePeersdavVec = new Vector();
        for (String key : k_dav) {
            RemotePeerDetails peerRmtDetailsDav = peerProcess.remotePeerDetailsMap.get(key);
            if (!key.equals(peerProcess.currentPeerID) && hasPeerInterested(peerRmtDetailsDav)) {
                detlsRemotePeersdavVec.add(peerRmtDetailsDav);
            }
        }

        if(!detlsRemotePeersdavVec.isEmpty()) {
            //randomize the list.
            Collections.shuffle(detlsRemotePeersdavVec);
            RemotePeerDetails peerRmtDetailsDav = detlsRemotePeersdavVec.firstElement();
            peerRmtDetailsDav.setIsOptimisticallyUnchockedNeighbor(1);
            peerProcess.optimisticUnchokedNeighbors.put(peerRmtDetailsDav.getId(), peerRmtDetailsDav);
            logAndShowInConsole(peerProcess.currentPeerID + " has the optimistically unchoked neighbor " + peerRmtDetailsDav.getId());

            if(peerRmtDetailsDav.getIsChoked() == 1) {
                //send unchoke message if choked
                peerProcess.remotePeerDetailsMap.get(peerRmtDetailsDav.getId()).setIsChoked(0);
                sendUnChokedMessage(peerProcess.peerToSocketMap.get(peerRmtDetailsDav.getId()), peerRmtDetailsDav.getId());
                sendHaveMessage(peerProcess.peerToSocketMap.get(peerRmtDetailsDav.getId()), peerRmtDetailsDav.getId());
                peerProcess.remotePeerDetailsMap.get(peerRmtDetailsDav.getId()).setPeerState(3);
            }
        }
    }

    /**
     * used to determine if peer is interested to receive pieces
     * @param peerRmtDetailsDav 
     * @return true - peer interested; false - peer not interested
     */
    private boolean hasPeerInterested(RemotePeerDetails peerRmtDetailsDav) {
        return peerRmtDetailsDav.getIsComplete() == 0 &&
                peerRmtDetailsDav.getIsChoked() == 1 && peerRmtDetailsDav.getIsInterested() == 1;
    }

    /**
     * Used to send UNCHOKED message to socket
     * @param socket 
     * @param remPID 
     */
    private void sendUnChokedMessage(Socket socket, String remPID) {
        logAndShowInConsole(peerProcess.currentPeerID + " sending a UNCHOKE message to Peer " + remPID);
        Message mssg = new Message(MessageConstants.MESSAGE_UNCHOKE);
        byte[] mssgBytes = Message.convertMessageToByteArray(mssg);
        SendMessageToSocket(socket, mssgBytes);
    }

    /**
     * Used to send HAVE message to socket
     * @param socket 
     * @param pID 
     */
    private void sendHaveMessage(Socket socket, String pID) {
        logAndShowInConsole(peerProcess.currentPeerID + " sending HAVE message to Peer " + pID);
        byte[] bytesBitField = peerProcess.bitFieldMessage.getBytes();
        Message mssg = new Message(MessageConstants.MESSAGE_HAVE, bytesBitField);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(mssg));
    }

    /**
     * Used to write a message to socket
     * @param socket 
     * @param messageInBytes 
     */
    private void SendMessageToSocket(Socket socket, byte[] messageInBytes) {
        try {
            OutputStream o_dav = socket.getOutputStream();
            o_dav.write(messageInBytes);
        } catch (IOException e) {
        }
    }

    /**
     * Used to log a message in a log file and display it in console
     * @param message 
     */
    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
