package pl.edu.agh.ki.sr.personel.Admin;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Admin {
    private MessageReceiver messageReceiver;
    private MessagePublisher messagePublisher;

    public Admin() throws IOException, TimeoutException {
        messageReceiver = new MessageReceiver();
        messagePublisher = new MessagePublisher();
    }

    public void listenOnHospital() throws IOException, TimeoutException {
        messageReceiver.startListeningOn();
    }

    public void publishMessages() {
        Thread publishThread = new Thread(messagePublisher);
        publishThread.start();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Admin admin = new Admin();
        admin.listenOnHospital();
        admin.publishMessages();
    }


}
