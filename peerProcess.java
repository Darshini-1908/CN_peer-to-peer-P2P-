import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to implement the P2P process to transfer file from peer to peer.
 */
@SuppressWarnings({"deprecation", "unchecked"})
public class peerProcess {
    //File server thread
    public Thread dav_svr_thread;
    //File server socket
    public ServerSocket dav_svr_skt = null;
    //peerID of current host
    public static String currentPeerID;
    //index of the current peer
    public static int dav_PIdx;
    //check if current peer has the peer having file initially
    public static boolean isFirstPeer = false;
    //port of the current peer
    public static int currentPeerPort;
    //check if current peer has the peer has file
    public static int currentPeerHasFile;
    //Bitfield of the current file
    public static BitFieldMessage bitFieldMessage = null;
    //Message processing thread
    public static Thread mssgProc;
    //check if file download is complete
    public static boolean downloaded_dav = false;
    //File receiver threads list
    public static Vector<Thread> peerThreads = new Vector();
    //File server threads list
    public static Vector<Thread> serverThreads = new Vector();
    //Timer to schedule determination of preferred neighbors
    public static volatile Timer neighbors_tp;
    //Timer to schedule determination of optimistically unchoked neighbors
    public static volatile Timer tOptUnchokedNeighbors;
    //store remote peer details
    public static volatile ConcurrentHashMap<String, RemotePeerDetails> remotePeerDetailsMap = new ConcurrentHashMap();
    //store preferred neighbors
    public static volatile ConcurrentHashMap<String, RemotePeerDetails> preferredNeighboursMap = new ConcurrentHashMap();
    //store peer sockets
    public static volatile ConcurrentHashMap<String, Socket> peerToSocketMap = new ConcurrentHashMap();
    //store optimistically unchoked neighbors
    public static volatile ConcurrentHashMap<String, RemotePeerDetails> optimisticUnchokedNeighbors = new ConcurrentHashMap();

    /**
     * Gets server thread
     * @return 
     */
    public Thread getServerThread() {
        return dav_svr_thread;
    }

    /**
     * Sets server thread
     * @param dav_svr_thread 
     */
    public void setServerThread(Thread dav_svr_thread) {
        this.dav_svr_thread = dav_svr_thread;
    }

    /**
     * Main method to run p2p file transfer. This method takes processID as input,
     * reads Common.cfg and PeerInfo.cfg and runs peerprocess to transfer files between peers
     */
    @SuppressWarnings({"deprecation", "unchecked"})
    public static void main(String[] args) {
        peerProcess process = new peerProcess();
        currentPeerID = args[0];

        try {
            //initialize logger and show started message in log file and console
            LogHelper logHelper = new LogHelper();
            logHelper.initializeLogger(currentPeerID);
            logAndShowInConsole(currentPeerID + " is started");

            //initialize peer, its neighbors, its preferred neighbors configuration configuration
            initializeConfiguration();

            //check if current peer is first peer(i.e, it initially has file)
            setCurrentPeerDetails();

            //initializing current peer bitfield information
            initializeBitFieldMessage();

            //starting the message processing thread
            startMessageProcessingThread(process);

            //starting the file server thread and file threads
            startFileServerReceiverThreads(process);

            //update preferred neighbors list
            determinePreferredNeighbors();

            //update optimistically unchoked neighbor list
            determineOptimisticallyUnchockedNeighbours();

            //if all the peers have completed downloading the file i.e, all entries in peerinfo.cfg update to 1 terminate current peer
            terminatePeer(process);

        } catch (Exception e) {
        } finally {
            logAndShowInConsole(currentPeerID + " Peer process is exiting..");
            System.exit(0);
        }
    }

    /**
     * Used to terminate peerprocess if all the peers have downloaded the files.
     * It terminates all the threads related to the peerprocess.
     * @param process 
     */
    private static void terminatePeer(peerProcess process) {
        while (true) {
            downloaded_dav = hasDownloadCompleted();
            if (downloaded_dav) {
                logAndShowInConsole("All peers have completed downloading the file.");
                neighbors_tp.cancel();
                tOptUnchokedNeighbors.cancel();

                try {
                    Thread.currentThread();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }

                if (process.getServerThread().isAlive()) {
                    process.getServerThread().stop();
                }

                if (mssgProc.isAlive()) {
                    mssgProc.stop();
                }

                for (Thread thread : peerThreads) {
                    if (thread.isAlive()) {
                        thread.stop();
                    }
                }

                for (Thread thread : serverThreads) {
                    if (thread.isAlive()) {
                        thread.stop();
                    }
                }

                break;

            } else {
                try {
                    Thread.currentThread();
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Used to initialze bitfield of the current peer
     */
    public static void initializeBitFieldMessage() {
        bitFieldMessage = new BitFieldMessage();
        bitFieldMessage.setPieceDetails(currentPeerID, currentPeerHasFile);
    }

    /**
     * Used to start file server and file receiver threads
     * @param process 
     */
    public static void startFileServerReceiverThreads(peerProcess process) {
        if (isFirstPeer) {
            //Peer having file initially. starting server thread
            startFileServerThread(process);
        } else {
            //if not a peer which has file initially. Create an empty new file. Starting serving and listening threads
            createNewFile();
            startFileReceiverThreads(process);
            startFileServerThread(process);
        }
    }

    /**
     * Used to start file receiver threads
     * @param process 
     */
    public static void startFileReceiverThreads(peerProcess process) {
        Set<String> remotePeerDetailsKeys = remotePeerDetailsMap.keySet();
        for (String pID : remotePeerDetailsKeys) {
            RemotePeerDetails remotePeerDetails = remotePeerDetailsMap.get(pID);

            if (process.dav_PIdx > remotePeerDetails.getIndex()) {
                Thread tempThread = new Thread(new PeerMessageHandler(
                        remotePeerDetails.getHostAddress(), Integer
                        .parseInt(remotePeerDetails.getPort()), 1,
                        currentPeerID));
                peerThreads.add(tempThread);
                tempThread.start();
            }
        }
    }

    /**
     * Used to start file server thread
     * @param process 
     */
    public static void startFileServerThread(peerProcess process) {
        try {
            //Start a new file server thread
            process.dav_svr_skt = new ServerSocket(currentPeerPort);
            process.dav_svr_thread = new Thread(new PeerServerHandler(process.dav_svr_skt, currentPeerID));
            process.dav_svr_thread.start();
        } catch (SocketTimeoutException e) {
            logAndShowInConsole(currentPeerID + " Socket Gets Timed out Error - " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Used to set current peer details
     */
    public static void setCurrentPeerDetails() {
        Set<String> remotePeerIDs = remotePeerDetailsMap.keySet();
        for (String pID : remotePeerIDs) {
            RemotePeerDetails remotePeerDetails = remotePeerDetailsMap.get(pID);
            if (remotePeerDetails.getId().equals(currentPeerID)) {
                currentPeerPort = Integer.parseInt(remotePeerDetails.getPort());
                dav_PIdx = remotePeerDetails.getIndex();
                if (remotePeerDetails.getHasFile() == 1) {
                    isFirstPeer = true;
                    currentPeerHasFile = remotePeerDetails.getHasFile();
                    break;
                }
            }
        }

    }

    /**
     * Used to initialize peer configuration and other peer details. Sets preferred neighbors
     * @throws Exception
     */
    public static void initializeConfiguration() throws Exception {

        //read Common.cfg
        initializePeerConfiguration();

        //read Peerinfo.cfg
        addOtherPeerDetails();

        //initialize preferred neighbours
        setPreferredNeighbours();

    }

    /**
     * creates a timer task to determine preferred neighbors
     */
    public static void determinePreferredNeighbors() {
        neighbors_tp = new Timer();
        neighbors_tp.schedule(new PrefNeighbors(),
                CommonConfiguration.unchockingInterval * 1000 * 0,
                CommonConfiguration.unchockingInterval * 1000);
    }

    /**
     * creates a timer task to determine optimistically unchoked neighbors
     */
    public static void determineOptimisticallyUnchockedNeighbours() {
        tOptUnchokedNeighbors = new Timer();
        tOptUnchokedNeighbors.schedule(new OptimisticallyUnchockedNeighbors(),
                CommonConfiguration.optimisticUnchokingInterval * 1000 * 0,
                CommonConfiguration.optimisticUnchokingInterval * 1000
        );
    }

    /**
     * Used to start message processing thread
     * @param process 
     */
    public static void startMessageProcessingThread(peerProcess process) {
        mssgProc = new Thread(new PeerMessageProcessingHandler(currentPeerID));
        mssgProc.start();
    }

    /**
     * Used to create empty file with size 'CommonConfiguration.fileSize' and set zero bits into it
     */
    public static void createNewFile() {
        try {
            File dir = new File(currentPeerID);
            dir.mkdir();

            File newfile = new File(currentPeerID, CommonConfiguration.fileName);
            OutputStream os = new FileOutputStream(newfile, true);
            byte b = 0;

            for (int i = 0; i < CommonConfiguration.fileSize; i++)
                os.write(b);
            os.close();
        } catch (Exception e) {
            logAndShowInConsole(currentPeerID + " ERROR in creating the file : " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Used to set preferred neighbors of a peer
     */
    public static void setPreferredNeighbours() {
        Set<String> remotePeerIDs = remotePeerDetailsMap.keySet();
        for (String pID : remotePeerIDs) {
            RemotePeerDetails remotePeerDetails = remotePeerDetailsMap.get(pID);
            if (remotePeerDetails != null && !pID.equals(currentPeerID)) {
                preferredNeighboursMap.put(pID, remotePeerDetails);
            }
        }
    }

    /**
     * Reads PeerInfo.cfg file and adds peers to remotePeerDetailsMap
     */
    public static void addOtherPeerDetails() throws IOException {
        try {
            List<String> lines = Files.readAllLines(Paths.get("PeerInfo.cfg"));
            for (int k = 0; k < lines.size(); k++) {
                String[] prop = lines.get(k).split("\\s+");
                remotePeerDetailsMap.put(prop[0],
                        new RemotePeerDetails(prop[0], prop[1], prop[2], Integer.parseInt(prop[3]), k));
            }
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Used to check if all the peers have downloaded the file
     * @return true - all peers downloaded the file; false - all peers did not download the file
     */
    public static synchronized boolean hasDownloadCompleted() {
        boolean downloaded_dav = true;
        try {
            List<String> lines = Files.readAllLines(Paths.get("PeerInfo.cfg"));
            for (int k = 0; k < lines.size(); k++) {
                String[] prop = lines.get(k).split("\\s+");
                if (Integer.parseInt(prop[3]) == 0) {
                    downloaded_dav = false;
                    break;
                }
            }
        } catch (IOException e) {
            downloaded_dav = false;
        }

        return downloaded_dav;
    }

    /**
     * Reads Common.cfg and initializes the properties in CommonConfiguration class
     * @throws IOException
     */
    public static void initializePeerConfiguration() throws IOException {
        try {
            List<String> lines = Files.readAllLines(Paths.get("Common.cfg"));
            for (String line : lines) {
                String[] prop = line.split("\\s+");
                if (prop[0].equalsIgnoreCase("NumberOfPreferredNeighbors")) {
                    CommonConfiguration.numberOfPreferredNeighbours = Integer.parseInt(prop[1]);
                } else if (prop[0].equalsIgnoreCase("UnchokingInterval")) {
                    CommonConfiguration.unchockingInterval = Integer.parseInt(prop[1]);
                } else if (prop[0].equalsIgnoreCase("OptimisticUnchokingInterval")) {
                    CommonConfiguration.optimisticUnchokingInterval = Integer.parseInt(prop[1]);
                } else if (prop[0].equalsIgnoreCase("FileName")) {
                    CommonConfiguration.fileName = prop[1];
                } else if (prop[0].equalsIgnoreCase("FileSize")) {
                    CommonConfiguration.fileSize = Integer.parseInt(prop[1]);
                } else if (prop[0].equalsIgnoreCase("PieceSize")) {
                    CommonConfiguration.pieceSize = Integer.parseInt(prop[1]);
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Used to log a message in a log file and show it in console
     * @param message 
     */
    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }

    /**
     * Reads PeerInfo.cfg file and updates peers in remotePeerDetailsMap
     */
    public static void updateOtherPeerDetails() {
        try {
            List<String> lines = Files.readAllLines(Paths.get("PeerInfo.cfg"));
            for (int k = 0; k < lines.size(); k++) {
                String[] prop = lines.get(k).split("\\s+");
                String pID = prop[0];
                int isCompleted = Integer.parseInt(prop[3]);
                if (isCompleted == 1) {
                    remotePeerDetailsMap.get(pID).setIsComplete(1);
                    remotePeerDetailsMap.get(pID).setIsInterested(0);
                    remotePeerDetailsMap.get(pID).setIsChoked(0);
                }
            }
        } catch (IOException e) {
        }
    }
}
