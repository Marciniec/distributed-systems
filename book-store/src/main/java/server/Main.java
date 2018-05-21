package server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import client.ClientActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        // config
        File configFile = new File("server.conf");
        Config config = ConfigFactory.parseFile(configFile);

        final ActorSystem system = ActorSystem.create("server_system", config);
        final ActorRef serverActor = system.actorOf(Props.create(Server.class), "server");
        serverActor.tell("start",null);

    }
}
