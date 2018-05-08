package pl.edu.agh.ki.sr.server.currency;

import pl.edu.agh.ki.sr.CurrencyType;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Rates {
    private static final Logger logger = Logger.getLogger(Rates.class.getName());

    private final Map<CurrencyType, Double> rates;

    public Rates(Map<CurrencyType, Double> rates) {
        this.rates = rates;
    }

    public double calculateWorth(CurrencyType from, CurrencyType to, double value) {
        logger.log(Level.INFO, "\u001B[36m" + "Calculating " + from + "*" + String.valueOf(value) + "/" + to + "\u001B[0m");
        return (rates.get(from) * value) / rates.get(to);
    }

    public CurrencyType fromIceToProtoCurrency(Bank.CurrencyType type) {
        switch (type) {
            case PLN:
                return CurrencyType.PLN;
            case USD:
                return CurrencyType.USD;
            case CHF:
                return CurrencyType.CHF;
            case EUR:
                return CurrencyType.EUR;
            default:
                throw new IllegalArgumentException();
        }
    }
}
