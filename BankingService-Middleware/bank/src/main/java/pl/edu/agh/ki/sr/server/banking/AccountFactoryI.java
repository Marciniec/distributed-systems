package pl.edu.agh.ki.sr.server.banking;

import Bank.AccountFactory;
import Bank.AccountPrx;
import Bank.InvalidValueError;
import Bank.PremiumAccountPrx;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.Identity;
import pl.edu.agh.ki.sr.server.currency.Rates;

import java.util.logging.Level;
import java.util.logging.Logger;


public class AccountFactoryI implements AccountFactory {
    private static final Logger logger = Logger.getLogger(AccountFactoryI.class.getName());

    private int premiumIncome = 6000;
    private Rates rates;

    public AccountFactoryI(Rates rates) {
        this.rates = rates;
    }

    @Override
    public AccountPrx createAccount(String firstName, String lastName, String pesel, double monthlyIncome, Current current) throws InvalidValueError {
        if (monthlyIncome < 0) throw new InvalidValueError("Income should be positive");
        if (monthlyIncome >= premiumIncome) {
            logger.log(Level.INFO, String.format("Creating PREMIUM account: %s, %s, %s,  %f", firstName, lastName, pesel, monthlyIncome));
            return PremiumAccountPrx.uncheckedCast(current.adapter.add(new PremiumAccountI(firstName, lastName, pesel, monthlyIncome, rates), new Identity(pesel, "premium")));
        }
        logger.log(Level.INFO, String.format("Creating STANDARD account: %s, %s, %s,  %f", firstName, lastName, pesel, monthlyIncome));
        return AccountPrx.uncheckedCast(current.adapter.add(new AccountI(firstName, lastName, pesel, monthlyIncome), new Identity(pesel, "standard")));
    }


}
