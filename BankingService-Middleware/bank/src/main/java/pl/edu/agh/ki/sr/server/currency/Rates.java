package pl.edu.agh.ki.sr.server.currency;

import pl.edu.agh.ki.sr.CurrencyType;

import java.util.HashMap;
import java.util.Map;

public class Rates {
    private final Map<CurrencyType, Double> rates;

    public Rates(Map<CurrencyType, Double> rates) {
        this.rates = rates;
    }

    public double calculateWorth(CurrencyType from, CurrencyType to, double value) {
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
