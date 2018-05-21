package server;

import akka.Done;
import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import messages.StreamRequest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StreamRequest.class, streamRequest -> {
                    System.out.println("XD");
                    try (Stream<String> stream = Files.lines(Paths.get(String.format("books/database1/%s.txt", streamRequest.getTitle())))) {
                        final Materializer materializer = ActorMaterializer.create(getContext().getSystem());
                        Source<String, NotUsed> source = Source.from(stream.collect(Collectors.toList()));
                        source.map(String::toString)
                                .throttle(1, Duration.ofSeconds(1))
                                .runWith(Sink.actorRef(getSender(), "OK"), materializer);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }
}
