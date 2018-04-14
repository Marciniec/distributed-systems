package pl.edu.agh.ki.sr;

import org.jgroups.JChannel;

import java.util.Scanner;

public class Main {


    public static void main(String[] args) throws Exception {
//        System.setProperty("jgroups.bind_addr", "127.0.0.1");
        System.setProperty("java.net.preferIPv4Stack", "true");
        String channelName = "SRCHANNEL";
        String multicastAdress = "230.0.0.4";
        DistributedMap map = new DistributedMap(channelName);
        Scanner scanner = new Scanner(System.in);
        String input;
        String key;
        String value;
        while (!(input = scanner.nextLine()).equals("quit")) {
            switch (input) {
                case "put":
                    key = scanner.nextLine();
                    value = scanner.nextLine();
                    System.out.println(map.put(key, value));
                    break;
                case "remove":
                    key = scanner.nextLine();
                    System.out.println(map.remove(key));
                    break;
                case "contains":
                    key = scanner.nextLine();
                    System.out.println(map.containsKey(key));
                    break;
                case "get":
                    key = scanner.nextLine();
                    System.out.println(map.get(key));
                    break;
                case "state":
                    map.printState();
                    break;
                default:
                    System.out.println("Wrong operation try again");
                    break;

            }
        }
        map.close();
    }
}
