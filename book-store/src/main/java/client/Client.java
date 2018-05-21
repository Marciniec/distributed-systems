package client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import messages.OrderRequest;
import messages.SearchRequest;
import messages.StreamRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {


    public static void main(String[] args) {
        String clientName;
        // config
        File configFile = new File("client.conf");
        Config config = ConfigFactory.parseFile(configFile);

        // create actor system & actors
        final ActorSystem system = ActorSystem.create("client_system",config);

        // interaction
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Please insert your name");
            clientName = br.readLine();
            final ActorRef clientActor = system.actorOf(Props.create(ClientActor.class), clientName);
            while (true) {
                System.out.println("You can search, order and stream books");
                String line = br.readLine();
                if (line.equals("q")) {
                    break;
                }
                if (line.startsWith("search")){
                    SearchRequest searchRequest = new SearchRequest(line.split(" ")[1],clientName);
                    clientActor.tell(searchRequest, null);
                }
                if (line.startsWith("order")){
                    OrderRequest orderRequest = new OrderRequest(line.split(" ")[1],clientName);
                    clientActor.tell(orderRequest, null);
                }
                if (line.startsWith("stream")){
                    StreamRequest streamRequest = new StreamRequest(line.split(" ")[1],clientName);
                    clientActor.tell(streamRequest, null);
                }

            }
        }catch (IOException e) {
            e.printStackTrace();
        }

        system.terminate();
    }
}
