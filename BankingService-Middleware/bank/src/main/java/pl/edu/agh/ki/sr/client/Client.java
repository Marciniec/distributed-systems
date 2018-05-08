package pl.edu.agh.ki.sr.client;

import Bank.*;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.LocalException;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Client {
    public void start(String[] args) {
        int status = 0;
        Communicator communicator = null;

        String line = null;
        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));


        try {

            System.out.println("ENTER BANK NAME");
            String bankName = in.readLine();
            System.out.print("==> ");
            System.out.flush();
            System.out.println("ENTER PORT NUMBER");
            System.out.print("==> ");
            System.out.flush();
            String portStr = in.readLine();
            int port = Integer.valueOf(portStr);

            // 1. Inicjalizacja ICE
            communicator = Util.initialize(args);

            // 2. Uzyskanie referencji obiektu na podstawie linii w pliku konfiguracyjnym
            //Ice.ObjectPrx base = communicator.propertyToProxy("Calc1.Proxy");
            // 2. To samo co powy¿ej, ale mniej ³adnie
            ObjectPrx base = communicator.stringToProxy(String.format("bank/%s:tcp -h localhost -p %d:udp -h localhost -p %d", bankName, port, port));
            // 3. Rzutowanie, zawê¿anie
            AccountFactoryPrx obj = AccountFactoryPrx.checkedCast(base);
            if (obj == null) throw new Error("Invalid proxy");

            AccountFactoryPrx objOneway = (AccountFactoryPrx) obj.ice_oneway();
            AccountFactoryPrx objBatchOneway = (AccountFactoryPrx) obj.ice_batchOneway();
            AccountFactoryPrx objDatagram = (AccountFactoryPrx) obj.ice_datagram();
            AccountFactoryPrx objBatchDatagram = (AccountFactoryPrx) obj.ice_batchDatagram();

            // 4. Wywolanie zdalnych operacji

            System.out.print("==> ");
            System.out.flush();
            System.out.println("CREATE ACCOUNT OR LOG IN");
            line = in.readLine();
            AccountPrx account = null;
            if (line.equals("create")) {
                System.out.println("FIRST NAME");
                System.out.print("==> ");
                System.out.flush();
                String firstName = in.readLine();
                System.out.println("LAST NAME");
                System.out.print("==> ");
                System.out.flush();
                String lastName = in.readLine();
                System.out.println("PESEL");
                System.out.print("==> ");
                System.out.flush();
                String pesel = in.readLine();
                System.out.println("YOUR MONTHLY INCOME");
                System.out.print("==> ");
                System.out.flush();

                String income = in.readLine();
                double money = Double.valueOf(income);
                try {
                    account = obj.createAccount(firstName, lastName, pesel, money);

                } catch (InvalidValueError e) {
                    System.out.println(e.reason);
                    System.exit(1);
                }
                System.out.println("Created account");
            }
            if (line.equals("premium") || line.equals("standard")) {
                System.out.println("PESEL");
                String pesel = in.readLine();
                base = communicator.stringToProxy(String.format("%s/%s:tcp -h localhost -p %d:udp -h localhost -p %d", line, pesel, port, port));
                account = AccountPrx.checkedCast(base);
                if (account == null) {
                    throw new Error("Invalid proxy");
                }

            }

            do {
                try {
                    System.out.print("==> ");
                    System.out.flush();
                    line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.equals("balance")) {
                        double balance = account.getBalance();
                        System.out.println("Account balance: " + balance);

                    }
                    if (line.equals("deposit")) {
                        String money = in.readLine();
                        Double amount = Double.valueOf(money);
                        account.depositMoney(amount);
                        System.out.println("Deposited " + money + " PLN");

                    }
                    if (line.equals("credit")) {
                        PremiumAccountPrx premiumAccountPrx = PremiumAccountPrx.uncheckedCast(account);
                        if (premiumAccountPrx == null) {
                            throw new Error("Invalid proxy");
                        }
                        System.out.println("Enter amount and currency");
                        String money = in.readLine();
                        String[] creditInfo = money.split(" ");
                        double amount = Double.valueOf(creditInfo[0]);
                        CurrencyType currencyType = CurrencyType.valueOf(creditInfo[1]);
                        System.out.println("Enter start date (dd-mm-yyyy): ");
                        String startDate = in.readLine();
                        SimpleDateFormat f = new SimpleDateFormat("dd-mm-yyyy");
                        Date d = f.parse(startDate);
                        long startMilliseconds = d.getTime();
                        System.out.println("Enter end date (dd-mm-yyyy): ");
                        String endDate = in.readLine();
                        d = f.parse(endDate);
                        long endMilliseconds = d.getTime();
                        try {
                            Credit credit = premiumAccountPrx.getCredit(amount, currencyType, startMilliseconds, endMilliseconds);
                            System.out.println("YOUR CREDIT INFO");
                            System.out.println("COST: " + credit.domesticCost + " " + credit.domesticCurrency);
                            System.out.println("COST: " + credit.foreignCost + " " + credit.foreignCurrency);
                        }catch (InvalidValueError | DateError e){
                            System.out.println(e);
                            break;
                        }
                    } else if (line.equals("x")) {
                        // Nothing to do
                    }
                } catch (java.io.IOException ex) {
                    System.err.println(ex);
                }
            }
            while (!line.equals("x"));


        } catch (LocalException e) {
            e.printStackTrace();
            status = 1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            status = 1;
        }
        if (communicator != null) {
            // Clean up
            //
            try {
                communicator.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                status = 1;
            }
        }
        System.exit(status);
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start(args);
    }
}
