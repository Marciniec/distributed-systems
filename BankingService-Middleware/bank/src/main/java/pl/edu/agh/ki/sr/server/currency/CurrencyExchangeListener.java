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

public class CurrencyExchangeListener {

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
        System.out.println("Listening on currencies: ");
        for (CurrencyType c :
                currencies) {
            System.out.print(c + " ");
        }

        Currencies request =
                Currencies.newBuilder()
                        .addAllType(Arrays.asList(currencies))
                        .build();
        Iterator<CurrencyExchange> currencyExchanges;
        try {
            currencyExchanges = exchangeServiceBlockingStub.getCurrencyRate(request);
            for (int i = 1; currencyExchanges.hasNext(); i++) {
                CurrencyExchange exchange = currencyExchanges.next();
                System.out.println(String.format("Received %s in rate of %s", exchange.getType(), exchange.getExchangeRate()));
                rates.put(exchange.getType(), exchange.getExchangeRate());
            }
        } catch (StatusRuntimeException e) {
            System.out.println(String.format("RPC failed: %s", e.getStatus()));
        }
    }

}
