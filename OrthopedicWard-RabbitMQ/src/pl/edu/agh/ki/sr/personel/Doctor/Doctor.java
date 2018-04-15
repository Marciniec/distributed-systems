package pl.edu.agh.ki.sr.personel.Doctor;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Doctor {
    private Channel publishChannel;
    private Channel receiveChannel;
    private String name;
    private static final String EXCHANGE_COMMISSION_NAME = "ExamCommissionExchange";
    private static final String EXCHANGE_RESULT_NAME = "ExamResultExchange";

    public Doctor() throws IOException, TimeoutException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        name = bufferedReader.readLine();

        // connection & publishChannel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        publishChannel = connection.createChannel();
        receiveChannel = connection.createChannel();

        // exchange
        publishChannel.exchangeDeclare(EXCHANGE_COMMISSION_NAME, BuiltinExchangeType.TOPIC);
        receiveChannel.exchangeDeclare(EXCHANGE_RESULT_NAME, BuiltinExchangeType.TOPIC);
    }

    private void commissionExam() {
        MedicalExamCommissionPublisher medicalExamCommissionPublisher = new MedicalExamCommissionPublisher(publishChannel,name);
        new Thread(medicalExamCommissionPublisher).start();
    }

    private void listenOnForResults() throws IOException {
        MedicalExamResultListener medicalExamResultListener = new MedicalExamResultListener(publishChannel,name);
        medicalExamResultListener.receiveMessages();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Doctor doctor = new Doctor();
        doctor.commissionExam();
        doctor.listenOnForResults();
    }


}
