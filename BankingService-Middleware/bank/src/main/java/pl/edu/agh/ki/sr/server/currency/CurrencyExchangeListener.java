package pl.edu.agh.ki.sr.server.currency;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import pl.edu.agh.ki.sr.CurrencyExchange;
import pl.edu.agh.ki.sr.ExchangeServiceGrpc;

import java.util.concurrent.TimeUnit;

public class CurrencyExchangeListener {
    private final ManagedChannel channel;
    private final ExchangeServiceGrpc.ExchangeServiceBlockingStub exchangeServiceBlockingStub;
    private final ExchangeServiceGrpc.ExchangeServiceStub exchangeServiceStub;

    public CurrencyExchangeListener(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();
        exchangeServiceBlockingStub = ExchangeServiceGrpc.newBlockingStub(channel);

        exchangeServiceStub = ExchangeServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }


}
