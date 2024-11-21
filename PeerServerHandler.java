import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Handles the File server thread.
 */
public class PeerServerHandler implements Runnable {
    
    private ServerSocket svr_skt_dav;
    
    private String dav_PID;
   
    private Socket diff_PSKT_dav;
    
    private Thread diff_PeerThread_dav;

    /**
     * Constructor to initialize File Server class with server socket and peerID of current peer
     * @param svr_skt_dav - socket of File server
     * @param dav_PID - peerID of the current peer
     */
    public PeerServerHandler(ServerSocket svr_skt_dav, String dav_PID) {
        this.svr_skt_dav = svr_skt_dav;
        this.dav_PID = dav_PID;
    }

    /**
     * This method runs every time the FileServer Thread starts. It accepts incoming socket connections
     * and starts threads to process messages.
     */
    @Override
    public void run() {
        while(true) {
            try{
                //accept incoming socket connections
                diff_PSKT_dav = svr_skt_dav.accept();
                //start a thread to handle incoming messages
                diff_PeerThread_dav = new Thread(new PeerMessageHandler(diff_PSKT_dav, 0, dav_PID));
                peerProcess.serverThreads.add(diff_PeerThread_dav);
                diff_PeerThread_dav.start();
            }catch (IOException e) {
// Handle IOException
            }
        }
    }

    /**
     * Logs a message in a log file and displays it in the console.
     *
     * @param message - message to be logged and showed in console
     */
    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
