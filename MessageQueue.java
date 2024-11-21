import java.util.LinkedList;
import java.util.Queue;

/**
 * establishes a message queue for processing incoming messages from a socket.
 */
public class MessageQueue {

     /**
     * Queue designed for processing messages received from a socket.
     */
    public static Queue<MessageDetails> msg_qu_detail_dav = new LinkedList();

     /**
     * Adds a new message to the message queue.
     * @param messageDetails - The message to be included.
     */
    public static synchronized void addMessageToMessageQueue(MessageDetails messageDetails)
    {
        msg_qu_detail_dav.add(messageDetails);
    }

   /**
     * Retrieves and removes a message from the message queue.
     * @return The message present in the queue, or null if the queue is empty.
     */
    public static synchronized MessageDetails getmsgFrmQueueDav() {
        return  !msg_qu_detail_dav.isEmpty() ? msg_qu_detail_dav.remove() : null;
    }
    
}