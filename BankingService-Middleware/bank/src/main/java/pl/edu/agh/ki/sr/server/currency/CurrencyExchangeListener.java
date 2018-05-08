package pl.edu.agh.ki.sr.server.currency;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pl.edu.agh.ki.sr.Currencies;
import pl.edu.agh.ki.sr.CurrencyExchange;
import pl.edu.agh.ki.sr.CurrencyType;
import pl.edu.agh.ki.sr.ExchangeServiceGrpc;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CurrencyExchangeListener {
    private static final Logger logger = Logger.getLogger(CurrencyExchangeListener.class.getName());

    private final ManagedChannel channel;
    private final ExchangeServiceGrpc.ExchangeServiceBlockingStub exchangeServiceBlockingStub;
    private final Map<CurrencyType, Double> rates;

    public CurrencyExchangeListener(String host, int port, Map<CurrencyType, Double> rates) {
        this.rates = rates;
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();
        exchangeServiceBlockingStub = ExchangeServiceGrpc.newBlockingStub(channel);

    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void startListeningOnCurrencyExchange(CurrencyType[] currencies) throws InterruptedException {
        logger.log(Level.INFO, "Listening on currencies: " + Arrays.toString(currencies)) ;


        Currencies request =
                Currencies.newBuilder()
                        .addAllType(Arrays.asList(currencies))
                        .build();
        Iterator<CurrencyExchange> currencyExchanges;
        try {
            currencyExchanges = exchangeServiceBlockingStub.getCurrencyRate(request);
            for (int i = 1; currencyExchanges.hasNext(); i++) {
                CurrencyExchange exchange = currencyExchanges.next();
                logger.info(String.format("Received %s in rate of %s", exchange.getType(), exchange.getExchangeRate()));
                rates.put(exchange.getType(), exchange.getExchangeRate());
            }
        } catch (StatusRuntimeException e) {
            logger.log(Level.SEVERE,String.format("RPC failed: %s", e.getStatus()));
        }
    }

}
