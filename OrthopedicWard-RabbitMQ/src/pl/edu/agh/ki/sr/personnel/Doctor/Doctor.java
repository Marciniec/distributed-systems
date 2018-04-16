package pl.edu.agh.ki.sr.personnel.Doctor;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

import static pl.edu.agh.ki.sr.config.Config.EXCHANGE_COMMISSION_NAME;
import static pl.edu.agh.ki.sr.config.Config.EXCHANGE_RESULT_NAME;
import static pl.edu.agh.ki.sr.config.Config.EXCHANGE_ADMIN_NAME;

public class Doctor {

    private String name;
    private Channel publishChannel;
    private Channel adminChannel;


    public Doctor() throws IOException, TimeoutException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please enter the name:");
        name = bufferedReader.readLine();

        // connection & publishChannel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();

        publishChannel = connection.createChannel();
        Channel receiveChannel = connection.createChannel();
        adminChannel = connection.createChannel();

        initAdminChannel();

        // exchange
        publishChannel.exchangeDeclare(EXCHANGE_COMMISSION_NAME, BuiltinExchangeType.TOPIC);
        receiveChannel.exchangeDeclare(EXCHANGE_RESULT_NAME, BuiltinExchangeType.TOPIC);
    }

    private void initAdminChannel() throws IOException {
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

    private void commissionExam() {
        MedicalExamCommissionPublisher medicalExamCommissionPublisher = new MedicalExamCommissionPublisher(publishChannel, name);
        new Thread(medicalExamCommissionPublisher).start();
    }

    private void listenOnForResults() throws IOException {
        MedicalExamResultListener medicalExamResultListener = new MedicalExamResultListener(publishChannel, name);
        medicalExamResultListener.receiveMessages();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Doctor doctor = new Doctor();
        doctor.commissionExam();
        doctor.listenOnForResults();
    }


}
