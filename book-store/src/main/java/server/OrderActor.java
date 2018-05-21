package server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.OrderRequest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class OrderActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderRequest.class, orderRequest -> {
                    saveToFile(orderRequest.getTitle());
                    getSender().tell("done", getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private void saveToFile(String title) throws IOException {
        try (FileWriter fw = new FileWriter("order.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(title);
        } catch (IOException e) {
            throw new IOException();
        }
    }
}
