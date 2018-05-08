package pl.edu.agh.ki.sr.server.banking;

import Bank.Credit;
import Bank.CurrencyType;
import Bank.PremiumAccount;
import com.zeroc.Ice.Current;
import pl.edu.agh.ki.sr.server.currency.Rates;

import static Bank.CurrencyType.PLN;

public class PremiumAccountI extends AccountI implements PremiumAccount {

    private Rates rates;

    PremiumAccountI(String firstName, String lastName, String pesel, double income, Rates rates) {
        super(firstName, lastName, pesel, income);
        this.rates = rates;
    }

    @Override
    public Credit getCredit(double amount, CurrencyType currency, long startDate, long endDate, Current current) {
        if (amount < 0) return null;
        double cost;
        double domesticCost;
        double percentage = 1.12;
        cost = amount * percentage;
        domesticCost = rates.calculateWorth(rates.fromIceToProtoCurrency(currency), rates.fromIceToProtoCurrency(PLN), cost);
        return new Credit(currency, cost, PLN, domesticCost);
    }


}
