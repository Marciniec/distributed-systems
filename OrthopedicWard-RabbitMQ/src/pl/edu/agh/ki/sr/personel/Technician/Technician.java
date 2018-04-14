package pl.edu.agh.ki.sr.personel.Technician;

import com.rabbitmq.client.*;
import pl.edu.agh.ki.sr.injuries.Injury;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Technician {

    private static final String EXCHANGE_COMMISSION_NAME = "ExamCommissionExchange";
    private static final String EXCHANGE_RESULT_NAME = "exchangeExamResult";

    private Injury specialisation1;
    private Injury specialisation2;
    private Connection connection;
    private Channel channel1;
    private Channel channel2;
    private Channel channelPublisher;

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
            channelPublisher = connection.createChannel();
            channel1 = connection.createChannel();
            channel2 = connection.createChannel();
            initChannelForInjury(channel1, specialisation1);
            initChannelForInjury(channel2, specialisation2);

        } catch (IllegalArgumentException e) {
            System.out.println("Wrong specialisation name");
        }
    }

    private void initChannelForInjury(Channel channel, Injury injury) throws IOException {
        channel.exchangeDeclare(EXCHANGE_COMMISSION_NAME, BuiltinExchangeType.TOPIC);

        // queue & bind
        String queueName = injury.name();
        channel.queueDeclare(queueName, false, false, false, null);
        String routingKey = "hospital.tech." + injury.name();
        System.out.println(routingKey);
        channel.queueBind(queueName, EXCHANGE_COMMISSION_NAME, routingKey);
        System.out.println("created queue: " + queueName);

        // consumer (message handling)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        // start listening
        System.out.println("Waiting for messages...");
        channel.basicConsume(queueName, false, consumer);

    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Technician technician = new Technician();
        technician.startTechnician();
    }
}

