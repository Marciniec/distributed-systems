package pl.edu.agh.ki.sr.personel.Admin;


import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class MessagePublisher implements Runnable {

    private final static String EXCHANGE_ADMIN_NAME = "AdminExchange";
    private Channel channel;

    public MessagePublisher() throws IOException, TimeoutException {
        // connection & publishChannel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_ADMIN_NAME, BuiltinExchangeType.FANOUT);

    }

    @Override
    public void run() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String message;
        while (true) {
            // read msg
            System.out.println("Enter message: ");
            try {
                message = bufferedReader.readLine();
                // break condition
                if ("exit".equals(message)) {
                    break;
                }

                // publish
                channel.basicPublish(EXCHANGE_ADMIN_NAME, "", null, message.getBytes("UTF-8"));
                System.out.println("Sent: " + message);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }
}

