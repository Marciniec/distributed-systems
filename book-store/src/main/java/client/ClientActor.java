package client;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.OrderRequest;
import messages.Request;
import messages.SearchRequest;
import messages.SearchResponse;

public class ClientActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, s -> {
                    getContext().actorSelection("akka.tcp://server_system@127.0.0.1:2553/user/server").tell(s, getSelf());
                })
                .match(SearchResponse.class, searchResponse -> {
                    if(searchResponse.getPrice()==-1) System.out.println("No such book");
                    else{
                        System.out.println(String.format("Cost is: %s", searchResponse.getPrice()));
                    }
                })
                .match(String.class, System.out::println)
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }
}
