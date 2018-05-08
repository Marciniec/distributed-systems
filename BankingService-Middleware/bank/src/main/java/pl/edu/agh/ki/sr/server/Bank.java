package pl.edu.agh.ki.sr.server;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import pl.edu.agh.ki.sr.CurrencyType;
import pl.edu.agh.ki.sr.server.banking.AccountFactoryI;
import pl.edu.agh.ki.sr.server.currency.CurrencyExchangeListener;
import Bank.AccountFactory;
import pl.edu.agh.ki.sr.server.currency.Rates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Bank {
    private int bankPort;
    private String bankName;
    private CurrencyType[] handledCurrencies;
    private Map<CurrencyType, Double> rates;
    private final CurrencyExchangeListener listener;
    private Rates ratesHandler;

    public Bank(String bankName, CurrencyType[] handledCurrencies, Map<CurrencyType, Double> rates, int bankPort) throws InterruptedException {
        this.bankName = bankName;
        this.handledCurrencies = handledCurrencies;
        this.bankPort = bankPort;
        this.rates = rates;
        listener = new CurrencyExchangeListener("localhost", 50051, rates);
        ratesHandler = new Rates(this.rates);
    }

    public void startBanking(String[] args) {
        int status = 0;
        Communicator communicator = null;

        try {
            // 1. Inicjalizacja ICE - utworzenie communicatora
            communicator = Util.initialize(args);
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Adapter1",
                    String.format("tcp -h localhost -p %s:udp -h localhost -p %s", bankPort, bankPort));

            // 3. Stworzenie serwanta/serwantów
            AccountFactory accountFactory = new AccountFactoryI(ratesHandler);

            // 4. Dodanie wpisów do tablicy ASM
            adapter.add(accountFactory, new Identity(bankName, "bank"));


            // 5. Aktywacja adaptera i przejcie w pêtlê przetwarzania ¿¹dañ
            adapter.activate();

            System.out.println("Entering event processing loop...");
            listener.startListeningOnCurrencyExchange(handledCurrencies);

            communicator.waitForShutdown();

        } catch (Exception e) {
            System.err.println(e);
            status = 1;
        }
        if (communicator != null) {
            // Clean up
            //
            try {
                communicator.destroy();
            } catch (Exception e) {
                System.err.println(e);
                status = 1;
            }
        }
        System.exit(status);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Give name to bank");
        String bankName = bufferedReader.readLine();
        System.out.println("Enter port");
        String portStr = bufferedReader.readLine();
        int port = Integer.valueOf(portStr);
        System.out.println("Add handled currencies");
        String currencies = bufferedReader.readLine();
        String[] curr = currencies.split(" ");
        CurrencyType[] handledCurrencies = new CurrencyType[curr.length];
        Map<CurrencyType, Double> ratesMap = new HashMap<>();
        for (int i = 0; i < curr.length; i++) {
            handledCurrencies[i] = CurrencyType.valueOf(curr[i]);
            ratesMap.put(handledCurrencies[i], null);
        }
        Bank bank = new Bank(bankName, handledCurrencies, ratesMap, port);
        bank.startBanking(args);

    }
}



