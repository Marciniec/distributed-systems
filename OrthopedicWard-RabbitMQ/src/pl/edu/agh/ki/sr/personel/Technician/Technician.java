package pl.edu.agh.ki.sr.personel.Technician;

import com.rabbitmq.client.*;
import pl.edu.agh.ki.sr.injuries.Injury;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Technician {

    private static final String EXCHANGE_COMMISSION_NAME = "ExamCommissionExchange";
    private static final String EXCHANGE_RESULT_NAME = "ExamResultExchange";
    private final static String EXCHANGE_ADMIN_NAME = "AdminExchange";

    private Injury specialisation1;
    private Injury specialisation2;
    private Connection connection;
    private Channel receiveChannel;
    private Channel publishChannel;
    private Channel adminChannel;

    private void startTechnician() throws IOException, TimeoutException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {

            System.out.println("Write first specialisation: ");
            specialisation1 = Injury.valueOf(bufferedReader.readLine());

            System.out.println("Write second specialisation: ");
            specialisation2 = Injury.valueOf(bufferedReader.readLine());

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            connection = factory.newConnection();

            publishChannel = connection.createChannel();
            publishChannel.exchangeDeclare(EXCHANGE_RESULT_NAME, BuiltinExchangeType.TOPIC);

            receiveChannel = connection.createChannel();

            initChannelForInjury(receiveChannel, specialisation1);
            initChannelForInjury(receiveChannel, specialisation2);
            initAdminChannel();

        } catch (IllegalArgumentException e) {
            System.out.println("Wrong specialisation name");
        }
    }


    private void initAdminChannel() throws IOException {
        adminChannel = connection.createChannel();
        adminChannel.exchangeDeclare(EXCHANGE_ADMIN_NAME, BuiltinExchangeType.FANOUT);
        String queueName = adminChannel.queueDeclare().getQueue();
        adminChannel.queueBind(queueName, EXCHANGE_ADMIN_NAME, "");
        Consumer consumer = new DefaultConsumer(adminChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
            }
        };
        // start listening
        adminChannel.basicConsume(queueName, false, consumer);

    }

    private void initChannelForInjury(Channel channel, Injury injury) throws IOException {
        channel.exchangeDeclare(EXCHANGE_COMMISSION_NAME, BuiltinExchangeType.TOPIC);

        // queue & bind
        String queueName = injury.name();
        channel.queueDeclare(queueName, false, false, false, null);
        String routingKey = "hospital.tech." + injury.name();
        channel.queueBind(queueName, EXCHANGE_COMMISSION_NAME, routingKey);
        System.out.println("created queue: " + queueName);

        // consumer (message handling)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);

                String returnMessage = createMessageToReturn(message);
                publishChannel.basicPublish(EXCHANGE_RESULT_NAME, "hospital.doctor." + extractReceivingDoctorsName(message), null, returnMessage.getBytes("UTF-8"));
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

