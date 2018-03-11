package pl.edu.agh.distributedsystems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Client {
    private String nick;
    private String hostName = "localhost";
    private int portNumber = 12345;
    private Socket socket;
    private Thread sendingThread;
    private Thread receivingThread;
    private DatagramSocket datagramSocket;
    private InetAddress address;
    private MulticastSocket multicastSocket;
    private InetAddress group;

    public Client() throws IOException {
        socket = new Socket(hostName, portNumber);
        datagramSocket = new DatagramSocket(socket.getLocalPort());
        address = InetAddress.getByName(hostName);
        multicastSocket = new MulticastSocket(12348);
        group = InetAddress.getByName("230.0.0.0");
        multicastSocket.setBroadcast(true);
        multicastSocket.joinGroup(group);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                socket.close();
                datagramSocket.close();
                multicastSocket.close();
            } catch (IOException e) {
                System.out.println("Exception occurred during closing socket");
            }
        }));
    }

    private String readFromConsole() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private void initNick() {
        System.out.print("Enter your nick: ");
        nick = readFromConsole();
    }

    private String readLinesFromIn() {
        Scanner scanner = new Scanner(System.in);
        List<String> input = new ArrayList<>();
        String line;
        input.add(nick + "\n");
        do {
            line = scanner.nextLine();
            input.add(line + "\n");
        } while (!line.isEmpty());
        return input.stream().collect(Collectors.joining());
    }

    private void multicastSend() throws IOException {

        byte[] sendBuffer = readLinesFromIn().getBytes();
        DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, group, 12348);
        datagramSocket.send(packet);

    }

    private String[] extractMessage(DatagramPacket receivePacket) {
        String message = new String(receivePacket.getData());
        String senderNick = message.split("\n")[0];
        String response = message.replaceAll(senderNick + "\n", "");
        return new String[]{senderNick, response};
    }

    private void printMessage(String[] message) {
        System.out.println("\u001B[35m" + message[0] + ": " + "\u001B[0m" + message[1]);
    }

    private byte[] initReceiveBuffer() {
        byte[] receiveBuffer = new byte[4096];
        Arrays.fill(receiveBuffer, (byte) 0);
        return receiveBuffer;
    }

    private void multicastReceive() throws IOException {
        byte[] receiveBuffer = initReceiveBuffer();
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        multicastSocket.receive(receivePacket);
        printMessage(extractMessage(receivePacket));
    }

    private void udpSend() throws IOException {
        byte[] sendBuffer = readLinesFromIn().getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, portNumber);
        datagramSocket.send(sendPacket);
    }

    private void udpReceive() throws IOException {
        byte[] receiveBuffer = initReceiveBuffer();
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        datagramSocket.receive(receivePacket);
        printMessage(extractMessage(receivePacket));

    }

    private void initSendingThread() {
        sendingThread = new Thread(() -> {
            String message;
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                while (!Thread.currentThread().isInterrupted()) {

                    message = readFromConsole();
                    out.println(nick + "\n" + message);
                    if (message.equals("U")) {
                        udpSend();
                    } else if (message.equals("M")) {
                        multicastSend();
                    }
                }
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }

    private void initReceivingThread() {
        receivingThread = new Thread(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response;
                String senderNick;
                while (!Thread.currentThread().isInterrupted()) {
                    senderNick = in.readLine();
                    response = in.readLine();
                    switch (response) {
                        case "U":
                            udpReceive();
                            break;
                        case "M":
                            multicastReceive();
                            break;
                        default:
                            printMessage(new String[]{senderNick, response});
                            break;
                    }
                }
                socket.close();
                multicastSocket.leaveGroup(group);
                multicastSocket.close();
                datagramSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                        multicastSocket.leaveGroup(group);
                        multicastSocket.close();
                        datagramSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        );
    }

    private void startClient() {
        initNick();
        initSendingThread();
        initReceivingThread();
        receivingThread.start();
        sendingThread.start();
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.startClient();
    }

}
