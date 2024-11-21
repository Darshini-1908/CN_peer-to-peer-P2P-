/**
 * Represents information about a file piece including its presence, source peer, content, and index.
 */
public class FilePiece {
    // Indicates whether the piece is present (0 - not present, 1 - present)
    private int is_Present;
    // Peer ID from which the piece is retrieved
    private String from_pID;
    // Content of the piece
    private byte[] text_d;
    //Index of piece in the list of pieces
    private int sectionIdx_d;

    
    //Initializing file piece information with default values.
    
    public FilePiece() {
        text_d = new byte[CommonConfiguration.pieceSize];
        sectionIdx_d = -1;
        is_Present = 0;
        from_pID = null;
    }

    /**
     * Retrieves the status of piece presence.
     * @return 0 if the piece is not present; 1 if the piece is present
     */
    public int getIsPresent() {
        return is_Present;
    }

    /**
     * Sets the status of piece presence.
     * @param is_Present 
     */
    public void setIsPresent(int is_Present) {
        this.is_Present = is_Present;
    }

    /**
     * Retrieves the peer ID from where the piece is received.
     * @return 
     */
    public String getFromPeerID() {
        return from_pID;
    }

    /**
     * Sets the peer ID from where the piece is received.
     * @param from_pID- 
     */
    public void setFromPeerID(String from_pID) {
        this.from_pID = from_pID;
    }

    /**
     * Gets the text_d of piece
     * @return 
     */
    public byte[] getContent() {
        return text_d;
    }

    /**
     * Sets the text_d of piece
     * @param -
     */
    public void setContent(byte[] text_d) {
        this.text_d = text_d;
    }

    /**
     * Retrieves the index of the piece.
     * @return 
     */
    public int getPieceIndex() {
        return sectionIdx_d;
    }

    /**
     * Sets the index of the piece.
     * @param -
     */
    public void setPieceIndex(int sectionIdx_d) {
        this.sectionIdx_d = sectionIdx_d;
    }

    /**
     * Converts a byte array representing a file piece to a FilePiece object.
     * @param byteToObj 
     * @return 
     */
    public static FilePiece convertByteArrayToFilePiece(byte[] byteToObj) {
        // Extracts the bytes representing the piece index from the byte array
        byte[] byteInd_d = new byte[MessageConstants.PIECE_INDEX_LENGTH];
        // Creates a new FilePiece object to hold the extracted information
        FilePiece filePiece = new FilePiece();
        System.arraycopy(byteToObj, 0, byteInd_d, 0, MessageConstants.PIECE_INDEX_LENGTH);
        // Creates a byte array to hold the content of the FilePiece
        filePiece.setPieceIndex(PeerProcessUtils.convertByteArrayToInt(byteInd_d));
        filePiece.setContent(new byte[byteToObj.length - MessageConstants.PIECE_INDEX_LENGTH]);
        System.arraycopy(byteToObj, MessageConstants.PIECE_INDEX_LENGTH, filePiece.getContent(), 0, byteToObj.length - MessageConstants.PIECE_INDEX_LENGTH);
        // Returns the FilePiece object created from the byte array
        return filePiece;
    }
}
