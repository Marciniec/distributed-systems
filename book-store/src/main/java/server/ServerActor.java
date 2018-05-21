package server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.*;

import server.search.SearchActor;
import server.search.SearchQuery;

public class ServerActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef searchActor1;
    private ActorRef searchActor2;
    private ActorRef streamActor = getContext().actorOf(Props.create(StreamActor.class), "stream");

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, searchRequest -> {
                    searchActor1 = getContext().actorOf(Props.create(SearchActor.class), "search1");
                    searchActor2 = getContext().actorOf(Props.create(SearchActor.class), "search2");
                    searchActor1.tell(new SearchQuery(searchRequest.getTitle(), 1), getSender());
                    searchActor2.tell(new SearchQuery(searchRequest.getTitle(), 2), getSender());
                })
                .match(StreamRequest.class, streamRequest -> {
                    streamActor.tell(streamRequest, getSender());
                })
                .match(SearchResponse.class, searchResponse -> {
                    getContext().stop(searchActor1);
                    getContext().stop(searchActor2);
                    getSender().tell(searchResponse, getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }
}
