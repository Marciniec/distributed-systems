package pl.edu.agh.distributedsystems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;


public class Server {
    private int portNumber = 12345;
    private ServerSocket serverSocket;
    private DatagramSocket serverDatagramSocket;
    private CopyOnWriteArrayList<Socket> clients;

    Server() {
        clients = new CopyOnWriteArrayList<>();
    }

    private void broadcastTcpMessage(Socket clientSocket, String clientNick, String message) throws IOException {
        for (Socket client :
                clients) {
            if (!client.equals(clientSocket)) {
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                out.println(clientNick + "\n" + message);
            }

        }

    }

    private byte[] initReceiveBuffer() {
        byte[] receiveBuffer = new byte[4096];
        Arrays.fill(receiveBuffer, (byte) 0);
        return receiveBuffer;
    }

    private void broadcastUdpMessage(Socket clientSocket, byte[] sendBuffer) throws IOException {
        for (Socket client :
                clients) {
            if (!client.equals(clientSocket)) {
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
                        client.getInetAddress(), client.getPort());
                System.out.println(new String(sendBuffer));
                serverDatagramSocket.send(sendPacket);

            }

        }

    }

    void removeClosedSocket(Socket removedSocket) throws IOException {
        clients.remove(removedSocket);
        removedSocket.close();
    }

    public void runServer() throws IOException {
        try {
            // create socket
            serverSocket = new ServerSocket(portNumber);
            serverDatagramSocket = new DatagramSocket(portNumber);
            while (true) {
                // accept client
                Socket clientSocket = serverSocket.accept();
                System.out.println("client connected");

                new Thread(() -> {
                    try {
                        clients.add(clientSocket);
                        while (true) {
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            String clientNick = in.readLine();
                            String msg = in.readLine();
                            if (clientNick == null) {
                                removeClosedSocket(clientSocket);
                                break;
                            }
                            broadcastTcpMessage(clientSocket, clientNick, msg);
                            if (msg.equals("U")) {
                                System.out.println("Receiving UDP");
                                byte[] receiveBuffer = initReceiveBuffer();
                                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                                serverDatagramSocket.receive(receivePacket);
                                broadcastUdpMessage(clientSocket, receivePacket.getData());
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
                serverDatagramSocket.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.runServer();
    }
}
