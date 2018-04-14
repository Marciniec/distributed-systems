package pl.edu.agh.ki.sr.personel.Doctor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.rabbitmq.client.Channel;
import pl.edu.agh.ki.sr.injuries.Injury;

public class MedicalExamCommissionPublisher implements Runnable {

    private Channel channel;
    private BufferedReader bufferedReader;
    private static final String EXCHANGE_NAME = "ExamCommissionExchange";
    private String routingKey;

    public MedicalExamCommissionPublisher(Channel channel) {
        this.channel = channel;
        bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        routingKey = "";
    }

    private List<String> extractOrdersFromMessage(String message) throws IllegalArgumentException {
        List<String> commands = Arrays.asList(message.split(" "));
        Injury.valueOf(commands.get(0));
        return commands;
    }

    @Override
    public void run() {
        String message;
        List<String> commands;
        while (true) {
            // read msg
            System.out.println("Enter message: ");
            try {
                message = bufferedReader.readLine();
                // break condition
                if ("exit".equals(message)) {
                    break;
                }
                try {
                    commands = extractOrdersFromMessage(message);
                    routingKey = "hospital.tech." + commands.get(0);
                    System.out.println(routingKey);
                    message = commands.get(1);
                } catch (IllegalArgumentException e) {
                    System.out.println("Wrong exam name please write it again");
                    continue;
                }

                // publish
                channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                System.out.println("Sent: " + message);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }
}
