package server;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import messages.GoodbyeRequest;
import messages.OrderRequest;
import messages.Request;
import scala.concurrent.duration.Duration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Server extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);


    private Map<String, ActorRef> actors = new HashMap<>();
    private ActorRef orderActor = getContext().actorOf(Props.create(OrderActor.class), "orders");

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderRequest.class, orderRequest -> {
                            orderActor.tell(orderRequest, getSender());
                        }
                )
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

                ).match(String.class, s -> {
                    if (s.equals("start")) {
                        System.out.println("Started server");
                    }
                })
                .match(GoodbyeRequest.class, goodbyeRequest -> getContext().stop(actors.get(goodbyeRequest.getClient())))
                .matchAny(o -> log.info("received unknown message"))
                        .build();
                }

        private static SupervisorStrategy strategy
                = new OneForOneStrategy(10, Duration.create("1 minute"), DeciderBuilder
                .match(FileNotFoundException.class, e -> SupervisorStrategy.escalate())
                .match(IOException.class, e -> SupervisorStrategy.restart())
                .matchAny(o -> SupervisorStrategy.restart())
                .build());

        @Override
        public SupervisorStrategy supervisorStrategy () {
            return strategy;
        }

    }
