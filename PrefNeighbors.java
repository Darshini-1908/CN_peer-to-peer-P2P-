import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

/**
 * Determines preferred neighbors from a list of choked neighbors.
 */
public class PrefNeighbors extends TimerTask {
    /**
     * This method runs every time the PrefNeighbors timer task is invoked.
     * For a peer with the file, it determines preferred neighbors randomly.
     * For a peer without the file, it determines neighbors based on the download rate of peers.
     */
    public void run() {
        int cnt_intr_dav = 0;
        StringBuilder fav_neigh_dav = new StringBuilder();
        
        peerProcess.updateOtherPeerDetails();
        
        Set<String> dav_rmt_PIDs = peerProcess.remotePeerDetailsMap.keySet();
        for (String key_dav : dav_rmt_PIDs) {
            RemotePeerDetails remotePeerDetails = peerProcess.remotePeerDetailsMap.get(key_dav);
            if (!key_dav.equals(peerProcess.currentPeerID)) {
                if (remotePeerDetails.getIsComplete() == 0 && remotePeerDetails.getIsInterested() == 1) {
                    cnt_intr_dav++;
                } else if (remotePeerDetails.getIsComplete() == 1) {
                    peerProcess.preferredNeighboursMap.remove(key_dav);
                }
            }
        }

        if (cnt_intr_dav > CommonConfiguration.numberOfPreferredNeighbours) {
           
            if (!peerProcess.preferredNeighboursMap.isEmpty())
                peerProcess.preferredNeighboursMap.clear();
            List<RemotePeerDetails> pv = new ArrayList(peerProcess.remotePeerDetailsMap.values());
            int isCompleteFilePresent = peerProcess.remotePeerDetailsMap.get(peerProcess.currentPeerID).getIsComplete();
            if (isCompleteFilePresent == 1) {
                Collections.shuffle(pv);
            } else {
                Collections.sort(pv, new DownloadRateSorter(false));
            }
            int count = 0;
            for (int i = 0; i < pv.size(); i++) {
                if (count > CommonConfiguration.numberOfPreferredNeighbours - 1)
                    break;
                if (pv.get(i).getIsInterested() == 1 && !pv.get(i).getId().equals(peerProcess.currentPeerID)
                        && peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).getIsComplete() == 0) {
                    peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).setIsPreferredNeighbor(1);
                    peerProcess.preferredNeighboursMap.put(pv.get(i).getId(), peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()));

                    count++;

                    fav_neigh_dav.append(pv.get(i).getId()).append(",");
                    if (peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).getIsChoked() == 1) {
                        sendUnChokedMessage(peerProcess.peerToSocketMap.get(pv.get(i).getId()), pv.get(i).getId());
                        peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).setIsChoked(0);
                        sendHaveMessage(peerProcess.peerToSocketMap.get(pv.get(i).getId()), pv.get(i).getId());
                        peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).setPeerState(3);
                    }
                }
            }
        } else {
          
            dav_rmt_PIDs = peerProcess.remotePeerDetailsMap.keySet();
            for (String key_dav : dav_rmt_PIDs) {
                RemotePeerDetails remotePeerDetails = peerProcess.remotePeerDetailsMap.get(key_dav);
                if (!key_dav.equals(peerProcess.currentPeerID)) {
                    if (remotePeerDetails.getIsComplete() == 0 && remotePeerDetails.getIsInterested() == 1) {
                        if (!peerProcess.preferredNeighboursMap.containsKey(key_dav)) {
                            fav_neigh_dav.append(key_dav).append(",");
                            peerProcess.preferredNeighboursMap.put(key_dav, peerProcess.remotePeerDetailsMap.get(key_dav));
                            peerProcess.remotePeerDetailsMap.get(key_dav).setIsPreferredNeighbor(1);
                        }
                        if (remotePeerDetails.getIsChoked() == 1) {
                            sendUnChokedMessage(peerProcess.peerToSocketMap.get(key_dav), key_dav);
                            peerProcess.remotePeerDetailsMap.get(key_dav).setIsChoked(0);
                            sendHaveMessage(peerProcess.peerToSocketMap.get(key_dav), key_dav);
                            peerProcess.remotePeerDetailsMap.get(key_dav).setPeerState(3);
                        }
                    }
                }
            }
        }

        if (fav_neigh_dav.length() != 0) {
            fav_neigh_dav.deleteCharAt(fav_neigh_dav.length() - 1);
            logAndShowInConsole(peerProcess.currentPeerID + " has selected the preferred neighbors - " + fav_neigh_dav.toString());
        }
    }

    /**
     * This method is used to send UNCHOKE message to socket
     * @param socket - socket in which the message to be sent
     * @param remotePeerID - peerID to which the message should be sent
     */
    private static void sendUnChokedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(peerProcess.currentPeerID + " sending a UNCHOKE message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_UNCHOKE);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(message));
    }


    /**
     * send HAVE message to socket
     * @param socket - socket in which the message to be sent
     * @param peerID - peerID to which the message should be sent
     */
    private void sendHaveMessage(Socket socket, String peerID) {
        //logAndShowInConsole(peerProcess.currentPeerID + " sending HAVE message to Peer " + peerID);
        byte[] bitFieldInBytes = peerProcess.bitFieldMessage.getBytes();
        Message message = new Message(MessageConstants.MESSAGE_HAVE, bitFieldInBytes);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(message));

        bitFieldInBytes = null;
    }

    /**
     * write a message to socket
     * @param socket - socket in which the message to be sent
     * @param messageInBytes - message to be sent
     */
    private static void SendMessageToSocket(Socket socket, byte[] messageInBytes) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(messageInBytes);
        } catch (IOException e) {
        }
    }

    /**
     * log a message in a log file and show it in console
     * @param message - message to be logged and showed in console
     */
    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
