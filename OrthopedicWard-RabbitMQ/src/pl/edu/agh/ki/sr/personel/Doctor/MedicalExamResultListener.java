package pl.edu.agh.ki.sr.personel.Doctor;

import com.rabbitmq.client.*;

import java.io.IOException;

public class MedicalExamResultListener {
    private static final String EXCHANGE_RESULT_NAME = "ExamResultExchange";

    private final String queueName;
    private Channel channel;


    public MedicalExamResultListener(Channel channel, String doctorsName) throws IOException {

        this.channel = channel;
        queueName = "doctor_" + doctorsName;
        // queue & bind
        channel.queueDeclare(queueName, false, false, false, null);
        String routingKey = "hospital.doctor." + doctorsName;
        channel.queueBind(queueName, EXCHANGE_RESULT_NAME, routingKey);
    }

    public void receiveMessages() throws IOException {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        System.out.println("Waiting for messages...");
        channel.basicConsume(queueName, false, consumer);
    }

}
