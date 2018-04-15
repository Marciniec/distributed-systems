package pl.edu.agh.ki.sr.personel.Doctor;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Doctor {
    private Channel channel;
    private static final String EXCHANGE_COMMISSION_NAME = "ExamCommissionExchange";
    private static final String EXCHANGE_RESULT_NAME = "ExamResultExchange";

    public Doctor() throws IOException, TimeoutException {
        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        // exchange
        channel.exchangeDeclare(EXCHANGE_COMMISSION_NAME, BuiltinExchangeType.TOPIC);
    }

    private void commissionExam() {
        MedicalExamCommissionPublisher medicalExamCommissionPublisher = new MedicalExamCommissionPublisher(channel);
        new Thread(medicalExamCommissionPublisher).start();
    }

    private void listenOnForResults() {

    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Doctor doctor = new Doctor();
        doctor.commissionExam();
    }


}
