package it.polimi.raft;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
    public static void main(String[] args) {
        // Check if correct n. of arguments
        if (args.length !=2) {
            System.out.println("[!] Error, bad arguments \n");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]); // First parameter: interface port
        int id = Integer.parseInt(args[1]); // Second parameter: node id

        String url = "rmi://localhost:" + port + "/S" + id; // RMI url
        System.out.println("Connecting to server via " + url);

        try {
            RaftServant server = (RaftServant) Naming.lookup(url);
            server.voteRequest(0, 0, 0, 0); // Initial vote request
            server.appendEntry(0, 0, 0, 0, null, 0); // Initial action
        } catch (MalformedURLException me) {
            System.out.println(me.getMessage());
        } catch (RemoteException re) {
            System.out.println(re.getMessage());
        } catch (NotBoundException nbe) {
            System.out.println(nbe.getMessage());
        }
    }
}
