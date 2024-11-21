import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * BitFieldMessage class stores bitfield messages of peers representing file pieces.
 */
public class BitFieldMessage {

    // file pieces list
    private FilePiece[] pieces_of_file;
    // total file pieces
    private int total_pieces_d;

    //Constructor to initialize a bitfield message with file size and pieces according to the provided configuration.

    public BitFieldMessage() {
        // Calculating total number of file pieces based on file size and piece size
        Double fileSize = Double.parseDouble(String.valueOf(CommonConfiguration.fileSize));
        Double pieceSize = Double.parseDouble(String.valueOf(CommonConfiguration.pieceSize));
        total_pieces_d = (int) Math.ceil((double) fileSize / (double) pieceSize);
        pieces_of_file = new FilePiece[total_pieces_d];

        // Initializing file pieces
        for (int k = 0; k < total_pieces_d; k++) {
            pieces_of_file[k] = new FilePiece();
        }
    }

    /**
     * Returns the list of file pieces.
     * 
     * @return pieces_of_file 
     */
    public FilePiece[] getFilePieces() {
        return pieces_of_file;
    }

    /**
     * Sets the list of file pieces
     * 
     * @param pieces_of_file 
     */
    public void setFilePieces(FilePiece[] pieces_of_file) {
        this.pieces_of_file = pieces_of_file;
    }

    /**
     * Get the number of file pieces
     * 
     * @return total_pieces_d 
     */
    public int getNumberOfPieces() {
        return total_pieces_d;
    }

    /**
     * Set the number of file pieces
     * 
     * @param total_pieces_d 
     */
    public void setNumberOfPieces(int total_pieces_d) {
        this.total_pieces_d = total_pieces_d;
    }

    /**
     * Sets whether pieces of a file are present or not based on the provided information.
     * Accepts pID which is set for a particular file piece and file_d
     * 
     * @param pID  
     * @param file_d 
     */
    public void setPieceDetails(String pID, int file_d) {
        // Setting the presence of file pieces based on file_d for all pieces in the bitfield.
        for (FilePiece fp_d : pieces_of_file) {
            fp_d.setIsPresent(file_d == 1 ? 1 : 0);
            fp_d.setFromPeerID(pID);
        }
    }

    /**
     * Converts the bitfield message to a byte array.
     * 
     * @return Bitfield message as a byte array
     */
    public byte[] getBytes() {
        // Converts the bitfield message into a byte array
        int a = total_pieces_d / 8;
        if (total_pieces_d % 8 != 0)
            a = a + 1;
        byte[] kP = new byte[a];
        int tem = 0;
        int m = 0;
        int dr;
        for (dr = 1; dr <= total_pieces_d; dr++) {
            int tempP = pieces_of_file[dr - 1].getIsPresent();
            tem = tem << 1;
            if (tempP == 1) {
                tem = tem + 1;
            } else
                tem = tem + 0;

            if (dr % 8 == 0 && dr != 0) {
                kP[m] = (byte) tem;
                m++;
                tem = 0;
            }

        }
        if ((dr - 1) % 8 != 0) {
            int tempShift = ((total_pieces_d) - (total_pieces_d / 8) * 8);
            tem = tem << (8 - tempShift);
            kP[m] = (byte) tem;
        }
        return kP;
    }

    /**
     * Converts a byte array to a BitField message.
     * 
     * @param BitField 
     * @return - BitField message object
     */
    public static BitFieldMessage decodeMessage(byte[] BitField) {
        // Decodes a byte array into a BitField message object
        BitFieldMessage message_d = new BitFieldMessage();
        for (int k = 0; k < BitField.length; k++) {
            int m = 7;
            while (m >= 0) {
                int test = 1 << m;
                if (k * 8 + (8 - m - 1) < message_d.getNumberOfPieces()) {
                    if ((BitField[k] & (test)) != 0)
                    message_d.getFilePieces()[k * 8 + (8 - m - 1)].setIsPresent(1);
                    else
                    message_d.getFilePieces()[k * 8 + (8 - m - 1)].setIsPresent(0);
                }
                m--;
            }
        }

        return message_d;
    }

    /**
     * Retrieves the number of file pieces present in a peer.
     * 
     * @return number of file pieces present
     */
    public int getNumberOfPiecesPresent() {
        // Counts the number of file pieces present in the bitfield
        int m = 0;
        for (FilePiece fp_d : pieces_of_file) {
            if (fp_d.getIsPresent() == 1) {
                m++;
            }
        }

        return m;
    }

    /**
     * Checks if all the pieces of a file have been downloaded.
     * 
     * @return true if file has been downloaded; false otherwise
     */
    public boolean isFileDownloadComplete() {
        // Checks if all file pieces have been downloaded
        boolean isFileDownloaded = true;
        for (FilePiece fp_d : pieces_of_file) {
            if (fp_d.getIsPresent() == 0) {
                isFileDownloaded = false;
                break;
            }
        }

        return isFileDownloaded;
    }

    /**
     * Finds the index of the first piece present in a remote peer and not in the current peer's bitfield.
     * 
     * @param message_d 
     * @return Index of the first differing piece
     */
    public synchronized int getInterestingPieceIndex(BitFieldMessage message_d) {
        // Finds the index of the first piece present in a remote peer's bitfield but not in the current peer's bitfield
        int total_pieces_d = message_d.getNumberOfPieces();
        int interestp_d = -1;

        for (int k = 0; k < total_pieces_d; k++) {
            if (message_d.getFilePieces()[k].getIsPresent() == 1
                    && this.getFilePieces()[k].getIsPresent() == 0) {
                interestp_d = k;
                break;
            }
        }

        return interestp_d;
    }

    /**
     * Finds the index of the first piece present in a remote peer but not in the current peer.
     * 
     * @param message_d 
     * @return Index of second piece
     */
    public synchronized int getFirstDifferentPieceIndex(BitFieldMessage message_d) {
        // Retrieves the number of pieces in the current BitField and the received BitField.
        int onePieces_d = total_pieces_d;
        int twoPieces_d = message_d.getNumberOfPieces();
        int indP_d = -1;
        
        // Compares the number of pieces in the two BitFields
        if (twoPieces_d >= onePieces_d) {
            for (int k = 0; k < onePieces_d; k++) {
                if (pieces_of_file[k].getIsPresent() == 0 && message_d.getFilePieces()[k].getIsPresent() == 1) {
                    indP_d = k;
                    break;
                }
            }
        } else {
            for (int k = 0; k < twoPieces_d; k++) {
                if (pieces_of_file[k].getIsPresent() == 0 && message_d.getFilePieces()[k].getIsPresent() == 1) {
                    indP_d = k;
                    break;
                }
            }
        }
        // Returns the index of the first differing piece between the two BitFields
        return indP_d;
    }

    /**
     * Updates the current peer's BitField with a received file piece.
     * If complete file is downloaded it updates peerinfo.cfg to set file_d value of current peer to 1.
     * 
     * @param pID   
     * @param fp_d 
    
    **/

    public void updateBitFieldInformation(String pID, FilePiece fp_d) {
        // Gets the index of the received file piece
        int indP_d = fp_d.getPieceIndex();
        try {
            // Checks if the piece is already present in the BitField
            if (isPieceAlreadyPresent(indP_d)) {
                // Log message indicating the reception of an existing piece
                logAndShowInConsole(pID + " Piece is received");
            } else {
                String fileName = CommonConfiguration.fileName;
                
                // Creates a file object corresponding to the current peer
                File file = new File(peerProcess.currentPeerID, fileName);
                int set = indP_d * CommonConfiguration.pieceSize;
                // Opens the file for writing the received content
                RandomAccessFile fileAccRan_d = new RandomAccessFile(file, "rw");
                byte[] writeP_d = fp_d.getContent();
                fileAccRan_d.seek(set);
                fileAccRan_d.write(writeP_d);

                pieces_of_file[indP_d].setIsPresent(1);
                pieces_of_file[indP_d].setFromPeerID(pID);
                fileAccRan_d.close();
                // Log download details and BitField update information
                logAndShowInConsole(peerProcess.currentPeerID + " downloaded the PIECE " + indP_d
                        + " from Peer " + pID + ". Now the total pieces it contains is "
                        + peerProcess.bitFieldMessage.getNumberOfPiecesPresent());
                
                // Checks if the file download is complete
                if (peerProcess.bitFieldMessage.isFileDownloadComplete()) {
                    // update file download details
                    peerProcess.remotePeerDetailsMap.get(pID).setIsInterested(0);
                    peerProcess.remotePeerDetailsMap.get(pID).setIsComplete(1);
                    peerProcess.remotePeerDetailsMap.get(pID).setIsChoked(0);
                    peerProcess.remotePeerDetailsMap.get(pID).updatePeerDetails(peerProcess.currentPeerID, 1);
                    logAndShowInConsole(peerProcess.currentPeerID + " has DOWNLOADED the complete file.");
                }
            }
        } catch (IOException e) {
            logAndShowInConsole(peerProcess.currentPeerID + " EROR in updating bitfield " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if a piece is already present in the BitField.
     * 
     * @param indP_d 
     * @return true - piece is present; false - piece is not present
     */
    private boolean isPieceAlreadyPresent(int indP_d) {
        // Checks if a piece is already present in the BitField
        return peerProcess.bitFieldMessage.getFilePieces()[indP_d].getIsPresent() == 1;
    }

    /**
     * Logs a message in a log file and displays it in the console.
     * 
     * @param message 
     */
    private static void logAndShowInConsole(String message) {
        // Logs a message and displays it in the console
        LogHelper.logAndShowInConsole(message);
    }
}
