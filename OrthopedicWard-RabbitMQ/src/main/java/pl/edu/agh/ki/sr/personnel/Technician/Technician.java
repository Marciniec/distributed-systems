package pl.edu.agh.ki.sr.personnel.Technician;

import com.rabbitmq.client.*;
import pl.edu.agh.ki.sr.injuries.Injury;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

import static pl.edu.agh.ki.sr.config.Config.EXCHANGE_COMMISSION_NAME;
import static pl.edu.agh.ki.sr.config.Config.EXCHANGE_RESULT_NAME;
import static pl.edu.agh.ki.sr.config.Config.EXCHANGE_ADMIN_NAME;

public class Technician {


    private Connection connection;
    private Channel publishChannel;

    private static final String receiveRoutingKeyPart = "hospital.tech.";
    private static final String publishRoutingKeyPart = "hospital.doctor.";

    private void startTechnician() throws IOException, TimeoutException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {

            System.out.println("Write first specialisation: ");
            Injury specialisation1 = Injury.valueOf(bufferedReader.readLine());

            System.out.println("Write second specialisation: ");
            Injury specialisation2 = Injury.valueOf(bufferedReader.readLine());

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            connection = factory.newConnection();

            publishChannel = connection.createChannel();
            publishChannel.exchangeDeclare(EXCHANGE_RESULT_NAME, BuiltinExchangeType.TOPIC);

            Channel receiveChannel = connection.createChannel();

            initChannelForInjury(receiveChannel, specialisation1);
            initChannelForInjury(receiveChannel, specialisation2);
            initAdminChannel();

        } catch (IllegalArgumentException e) {
            System.out.println("Wrong specialisation name!");
        }
    }


    private void initAdminChannel() throws IOException {
        Channel adminChannel = connection.createChannel();
        adminChannel.exchangeDeclare(EXCHANGE_ADMIN_NAME, BuiltinExchangeType.FANOUT);

        String queueName = adminChannel.queueDeclare().getQueue();
        adminChannel.queueBind(queueName, EXCHANGE_ADMIN_NAME, "");

        Consumer consumer = new DefaultConsumer(adminChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("\u001B[31m" + "Received from admin: " + message + "\u001B[0m");
            }
        };
        // start listening
        adminChannel.basicConsume(queueName, false, consumer);

    }

    private void initChannelForInjury(final Channel channel, Injury injury) throws IOException {
        channel.exchangeDeclare(EXCHANGE_COMMISSION_NAME, BuiltinExchangeType.TOPIC);

        // queue & bind
        String queueName = injury.name();
        channel.queueDeclare(queueName, false, false, false, null);
        String routingKey = receiveRoutingKeyPart + injury.name();
        channel.queueBind(queueName, EXCHANGE_COMMISSION_NAME, routingKey);
        System.out.println("created queue: " + queueName);

        // consumer (message handling)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("\u001B[34m" + "Received from doctor: " + message + "\u001B[0m");

                String returnMessage = createMessageToReturn(message);
                publishChannel.basicPublish(EXCHANGE_RESULT_NAME, publishRoutingKeyPart + extractReceivingDoctorsName(message), null, returnMessage.getBytes("UTF-8"));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        // start listening
        System.out.println("Waiting for messages...");
        channel.basicConsume(queueName, false, consumer);

    }

    private String extractReceivingDoctorsName(String message) {
        return message.split(" ")[2];
    }

    private String createMessageToReturn(String message) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] extractedMessage = message.split(" ");
        stringBuilder.append(extractedMessage[1]).append(" ");
        stringBuilder.append(extractedMessage[0]).append(" ");
        stringBuilder.append("done");
        return stringBuilder.toString();

    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Technician technician = new Technician();
        technician.startTechnician();
    }
}

