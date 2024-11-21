import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
/**
 * Class to start remote peers and establish SSH connections.
 */
public class StartRemotePeers {

    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }

    private static final String dav_script_p = "cd cn_test2 && java peerProcess ";

    public static class PeerInfo {

        private String PID_a;
        private String dav_name_of_host;

        public PeerInfo(String PID_a, String dav_name_of_host) {
            super();
            this.PID_a = PID_a;
            this.dav_name_of_host = dav_name_of_host;
        }

        public String getPeerID() {
            return PID_a;
        }

        public void setPeerID(String PID_a) {
            this.PID_a = PID_a;
        }

        public String getHostName() {
            return dav_name_of_host;
        }

        public void setHostName(String dav_name_of_host) {
            this.dav_name_of_host = dav_name_of_host;
        }

    }

    public static void main(String[] args) {

        ArrayList<PeerInfo> peerList = new ArrayList<>();

        String ciseUser = "j.bheemavarapu"; // change with your CISE username

        /**
         * Make sure the below peer hostnames and peerIDs match those in
         * PeerInfo.cfg in the remote CISE machines. Also, make sure that the
         * peers which have the file initially have it under the 'peer_[peerID]'
         * folder.
         */
        // peerList.add(new PeerInfo("1001", "lin114-06.cise.ufl.edu"));
        try {
            List<String> lines = Files.readAllLines(Paths.get("PeerInfo.cfg"));
            for (int i = 0; i < lines.size(); i++) {
                String[] dav_features = lines.get(i).split("\\s+");
                String PID_a = dav_features[0];
                String dav_name_of_host = dav_features[1];
                peerList.add(new PeerInfo(PID_a, dav_name_of_host));
            }
        } catch (IOException e) {
        }
        // peerList.add(new PeerInfo("1001", "lin114-08.cise.ufl.edu"));
        // peerList.add(new PeerInfo("1002", "lin114-09.cise.ufl.edu"));
        // peerList.add(new PeerInfo("1003", "lin114-04.cise.ufl.edu"));
        // peerList.add(new PeerInfo("1004", "lin114-05.cise.ufl.edu"));

        for (PeerInfo remotePeer : peerList) {
            try {
                JSch jsch = new JSch();
               
                jsch.addIdentity("~/.ssh/id_rsa", "");
                Session session = jsch.getSession(ciseUser, remotePeer.getHostName(), 22);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);

                session.connect();

                System.out.println("Session to peer# " + remotePeer.getPeerID() + " at " + remotePeer.getHostName());
                logAndShowInConsole("Session to peer# " + remotePeer.getPeerID() + " at " + remotePeer.getHostName());

                Channel channel = session.openChannel("exec");
                System.out.println("remotePeerID" + remotePeer.getPeerID());
                ((ChannelExec) channel).setCommand(dav_script_p + remotePeer.getPeerID());

                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);

                InputStream input = channel.getInputStream();
                channel.connect();

                System.out.println("Channel Connected to peer# " + remotePeer.getPeerID() + " at "
                        + remotePeer.getHostName() + " server with commands");

                (new Thread() {
                    @Override
                    public void run() {

                        InputStreamReader inputReader = new InputStreamReader(input);
                        BufferedReader bufferedReader = new BufferedReader(inputReader);
                        String line = null;

                        try {

                            while ((line = bufferedReader.readLine()) != null) {
                                System.out.println(remotePeer.getPeerID() + ">:" + line);
                            }
                            bufferedReader.close();
                            inputReader.close();
                        } catch (Exception ex) {
                            System.out.println(remotePeer.getPeerID() + " Exception >:");
                            ex.printStackTrace();
                        }

                        channel.disconnect();
                        session.disconnect();
                    }
                }).start();

            } catch (JSchException e) {
                // TODO Auto-generated catch block
                System.out.println(remotePeer.getPeerID() + " JSchException >:");
                e.printStackTrace();
            } catch (IOException ex) {
                System.out.println(remotePeer.getPeerID() + " Exception >:");
                ex.printStackTrace();
            }

        }
    }

}
