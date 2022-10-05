package it.polimi.baccichetmagri.raft;

import java.util.Arrays;

public class App {
    public static void main(String[] args) {
        String[] specificArgs = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0]) {
            case "--server": Server.main(specificArgs); break;
            case "--client": Client.main(specificArgs); break;
            default: System.out.println("Please provide correct first argument.");
        }
    }
}
