import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used to store remote peer details information
 */
public class RemotePeerDetails {
    //Peer ID
    private String id;
    //Peer Address
    private String addressOfHost;
    //peer Port
    private String portPeer;
    //Check if peer has fule
    private int file_d;
    //peer index
    private int ind_d;
    //peer state
    private int peerState = -1;
    //Previous state of peer
    private int previousPeerState = -1;
    //Check if peer is preferred neighbor
    private int isPreferredNeighbor = 0;
    //Bitfield of the peer
    private BitFieldMessage bitFieldMessage;
    //Check if peer is isOptimisticallyUnchockedNeighbor
    private int isOptimisticallyUnchockedNeighbor;
    //Check if peer is interested
    private int isInterested;
    //Check if peer is HandShaked
    private int isHandShaked;
    //Check if peer is Choked
    private int isChoked;
    //Check if peer has complete file
    private int isComplete;
    //Start time of peer piece download
    private Date startTime;
    //End time of peer piece download
    private Date endTime;
    //Rate of piece download
    private double dataRate;

    /**
     * Constructor to initialize PeerDetails object
     */
    public RemotePeerDetails(String id, String addressOfHost, String portPeer, int file_d, int ind_d) {
        this.id = id;
        this.addressOfHost = addressOfHost;
        this.portPeer = portPeer;
        this.file_d = file_d;
        this.ind_d = ind_d;
        this.dataRate = 0;
        this.isOptimisticallyUnchockedNeighbor = 0;
    }

    /**
     * This method is used to get ID of the peer
     * @return 
     */
    public String getId() {
        return id;
    }

    /**
     * This method is used to set ID of the peer
     * @param id 
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * This method is used to get address of the peer
     * @return 
     */
    public String getHostAddress() {
        return addressOfHost;
    }

    /**
     * This method is used to set address of the peer
     * @param addressOfHost 
     */
    public void setHostAddress(String addressOfHost) {
        this.addressOfHost = addressOfHost;
    }

    /**
     * This method is used to get port of the peer
     * @return 
     */
    public String getPort() {
        return portPeer;
    }

    /**
     * This method is used to set port of the peer
     * @param portPeer 
     */
    public void setPort(String portPeer) {
        this.portPeer = portPeer;
    }

    /**
     * This method is used to check if peer has file
     * @return 1 - peer has file; 0 - peer does not have file
     */
    public int getHasFile() {
        return file_d;
    }

    /**
     * This method is used to set peer has file
     * @param file_d
     */
    public void setHasFile(int file_d) {
        this.file_d = file_d;
    }

    /**
     * This method is used to get index of the peer
     * @return 
     */
    public int getIndex() {
        return ind_d;
    }

    /**
     * This method is used to set index of the peer
     * @param ind_d of 
     */
    public void setIndex(int ind_d) {
        this.ind_d = ind_d;
    }

    /**
     * This method is used to check if peer is preferred neighbor
     * @return 1 - peer is preferred neighbor; 0 - peer is not preferred neighbor
     */
    public int getIsPreferredNeighbor() {
        return this.isPreferredNeighbor;
    }

    /**
     * This method is used to set preferred neighbor
     * @param isPreferredNeighbor
     */
    public void setIsPreferredNeighbor(int isPreferredNeighbor) {
        this.isPreferredNeighbor = isPreferredNeighbor;
    }

    /**
     * This method is used to get state of the peer
     * @return 
     */
    public int getPeerState() {
        return peerState;
    }

    /**
     * This method is used to set state of the peer
     * @param peerState 
     */
    public void setPeerState(int peerState) {
        this.peerState = peerState;
    }

    /**
     * This method is used to get bitfield of the current peer
     * @return 
     */
    public BitFieldMessage getBitFieldMessage() {
        return bitFieldMessage;
    }

    /**
     * This method is used to set bitfield of the current peer
     * @param bitFieldMessage 
     */
    public void setBitFieldMessage(BitFieldMessage bitFieldMessage) {
        this.bitFieldMessage = bitFieldMessage;
    }

    /**
     * This method is used to check if peer is interested to receive data
     * @return 1 - peer is interested; 0 - peer is not interested
     */
    public int getIsInterested() {
        return isInterested;
    }

    /**
     * This method is used to set peer interested to receive data
     * @param isInterested
     */
    public void setIsInterested(int isInterested) {
        this.isInterested = isInterested;
    }

    /**
     * This method is used to check if peer is handshaked
     * @return 1 - peer is handshaked; 0 - peer is not handshaked
     */
    public int getIsHandShaked() {
        return isHandShaked;
    }

    /**
     * This method is used to set peer is handshaked
     * @param isHandShaked
     */
    public void setIsHandShaked(int isHandShaked) {
        this.isHandShaked = isHandShaked;
    }

    /**
     * This method is used to check if peer is choked
     * @return 1 - peer is choked; 0 - peer is not choked
     */
    public int getIsChoked() {
        return isChoked;
    }

    /**
     * This method is used to set peer is choked
     * @param isChoked
     */
    public void setIsChoked(int isChoked) {
        this.isChoked = isChoked;
    }

    /**
     * This method get start time of file transfer
     * @return 
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * This method set start time of file transfer
     * @param startTime 
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * This method get end time of file transfer
     * @return 
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * This method set end time of file transfer
     * @param endTime 
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * This method is used to get data rate of file transfer
     * @return 
     */
    public double getDataRate() {
        return dataRate;
    }

    /**
     * This method is used to set data rate of file transfer
     * @param dataRate 
     */
    public void setDataRate(double dataRate) {
        this.dataRate = dataRate;
    }

    /**
     * This method is used to check if peer has received complete file
     * @return 1 - peer has received complete file; 0 - peer has not received complete file
     */
    public int getIsComplete() {
        return isComplete;
    }

    /**
     * This method is used to set peer if peer has received complete file
     * @param isComplete
     */
    public void setIsComplete(int isComplete) {
        this.isComplete = isComplete;
    }

    /**
     * This method is used to check if peer isOptimisticallyUnchockedNeighbor
     * @return 1 - optimisticallyUnchockedNeighbor; 0 - not optimisticallyUnchockedNeighbor
     */
    public int getIsOptimisticallyUnchockedNeighbor() {
        return isOptimisticallyUnchockedNeighbor;
    }

    /**
     * This method is used to set if peer isOptimisticallyUnchockedNeighbor
     * @param isOptimisticallyUnchockedNeighbor
     */
    public void setIsOptimisticallyUnchockedNeighbor(int isOptimisticallyUnchockedNeighbor) {
        this.isOptimisticallyUnchockedNeighbor = isOptimisticallyUnchockedNeighbor;
    }

    /**
     * This method is used to get previous peer state
     * @return 
     */
    public int getPreviousPeerState() {
        return previousPeerState;
    }

    /**
     * This method is used to set previous peer state
     * @param previousPeerState 
     */
    public void setPreviousPeerState(int previousPeerState) {
        this.previousPeerState = previousPeerState;
    }

    /**
     * This method is used to update peerID entry with hasFile parameter in PeerInfo.cfg file
     * @param currentPeerID 
     * @param file_d 
     * @throws IOException
     */
    public void updatePeerDetails(String currentPeerID, int file_d) throws IOException {
        Path path = Paths.get("PeerInfo.cfg");
        Stream<String> lines = Files.lines(path);

        List<String> newLines = lines.map(line ->
                {
                    String newLine = line;
                    String[] tokens = line.trim().split("\\s+");
                    if (tokens[0].equals(currentPeerID)) {
                        newLine = tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + file_d;
                    }

                    return newLine;
                }
        ).collect(Collectors.toList());
        Files.write(path, newLines);
        lines.close();
    }

    /**
     * This method is used to compare download rates of current peer with peer provided in params.
     * @param o1 
     * @return 
     */
    public int compareTo(RemotePeerDetails o1) {

        if (this.dataRate > o1.dataRate)
            return 1;
        else if (this.dataRate == o1.dataRate)
            return 0;
        else
            return -1;
    }
}
