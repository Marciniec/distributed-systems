package pl.edu.agh.ki.sr.server.banking;

import Bank.AccountFactory;
import Bank.AccountPrx;
import Bank.PremiumAccountPrx;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.Identity;
import pl.edu.agh.ki.sr.server.currency.Rates;


public class AccountFactoryI implements AccountFactory {
    private int premiumIncome = 6000;
    private Rates rates;

    public AccountFactoryI(Rates rates) {
        this.rates = rates;
    }

    @Override
    public AccountPrx createAccount(String firstName, String lastName, String pesel, double monthlyIncome, Current current) {
        if (monthlyIncome >= premiumIncome) {
            return PremiumAccountPrx.uncheckedCast(current.adapter.add(new PremiumAccountI(firstName, lastName, pesel, monthlyIncome, rates), new Identity(pesel, "premium")));
        }
        return AccountPrx.uncheckedCast(current.adapter.add(new AccountI(firstName, lastName, pesel, monthlyIncome), new Identity(pesel, "standard")));
    }


}
