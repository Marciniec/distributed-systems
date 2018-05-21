package server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.Request;

import java.util.HashMap;
import java.util.Map;

public class Server extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);


    private Map<String, ActorRef> actors = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, request -> {
                            if (actors.size() >= 50) getSender().tell("Server is running af full capacity sorry ", getSelf());
                            else {
                                ActorRef actor = actors.get(request.getClient());
                                if (actor != null) actor.forward(request, getContext());
                                else {
                                    actor = getContext().actorOf(Props.create(ServerActor.class), request.getClient());
                                    actors.put(request.getClient(), actor);
                                    actor.forward(request, getContext());
                                }
                            }
                        }

                ).match(String.class, s->{
                    if(s.equals("start")) {
                        System.out.println("Started server");
                    }
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

}
