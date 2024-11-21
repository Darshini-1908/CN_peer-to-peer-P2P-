/**
 * Contains configuration settings for a peer.
 */
public class CommonConfiguration {
    //Number of preferred neighbors of a peer
    public static int numberOfPreferredNeighbours;
    //Interval for preferred neighbors
    public static int unchockingInterval;
    //Interval for optimistic unchoking neighbors
    public static int optimisticUnchokingInterval;
    //Name of the file being shared
    public static String fileName;
    //Size of the file being shared
    public static int fileSize;
    //Size of each piece the file needs to be divided into
    public static int pieceSize;
}

