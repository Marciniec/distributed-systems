package pl.edu.agh.ki.sr.server.banking;

import Bank.*;
import com.zeroc.Ice.Current;
import pl.edu.agh.ki.sr.server.currency.Rates;

import java.util.logging.Level;
import java.util.logging.Logger;

import static Bank.CurrencyType.PLN;

public class PremiumAccountI extends AccountI implements PremiumAccount {

    private static final Logger logger = Logger.getLogger(PremiumAccountI.class.getName());

    private Rates rates;

    PremiumAccountI(String firstName, String lastName, String pesel, double income, Rates rates) {
        super(firstName, lastName, pesel, income);
        this.rates = rates;
    }

    @Override
    public Credit getCredit(double amount, CurrencyType currency, long startDate, long endDate, Current current) throws DateError, InvalidValueError {
        if (amount < 0) throw new InvalidValueError("You cannot take negative value credit");
        if (endDate - startDate < 0) throw new DateError("End date is earlier than start date");
        double cost;
        double domesticCost;
        double percentage = 1.12;
        logger.log(Level.INFO, "\u001B[32m" + String.format("Client %s %s asked for credit: %f %s", getFirstName(), getLastName(), amount, String.valueOf(currency)) + "\u001B[0m");
        cost = amount * percentage;
        domesticCost = rates.calculateWorth(rates.fromIceToProtoCurrency(currency), rates.fromIceToProtoCurrency(PLN), cost);
        return new Credit(currency, cost, PLN, domesticCost);
    }


}
