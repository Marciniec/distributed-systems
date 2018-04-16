package pl.edu.agh.ki.sr.personnel.Admin;

import com.rabbitmq.client.*;

import static pl.edu.agh.ki.sr.config.Config.EXCHANGE_COMMISSION_NAME;
import static pl.edu.agh.ki.sr.config.Config.EXCHANGE_RESULT_NAME;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MessageReceiver {


    private static final String commissionQueue = "AdminQueueCommission";
    private static final String resultQueue = "AdminQueueResult";
    private static final String routingKey = "hospital.#.#";

    public void startListeningOn() throws IOException, TimeoutException {
        initListening(EXCHANGE_RESULT_NAME, resultQueue);
        initListening(EXCHANGE_COMMISSION_NAME, commissionQueue);
    }


    private void initListening(String exchangeName, String queueName) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC);
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, exchangeName, routingKey);

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

}
