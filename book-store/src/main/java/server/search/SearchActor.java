package server.search;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.SearchResponse;

import java.io.*;

public class SearchActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchQuery.class,
                        s -> getContext().parent().tell(new SearchResponse(s.getBookName(), findPriceOfTitle(s.getBookName(), s.getDatabaseNumber())), getSender()))
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }


    private int findPriceOfTitle(String title, int numberOfDatabase) {
        File file = new File(String.format("books/database%d/book-index.txt", numberOfDatabase));
        String line = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(title)) {
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line != null ? Integer.valueOf(line.split(" ")[1]) : -1;


    }
}
