package pl.edu.agh.ki.sr.server.banking;

import com.zeroc.Ice.Current;
import Bank.Account;
public class AccountI implements Account {
    private String firstName;
    private String lastName;
    private String pesel;
    private double balance;
    private double income;

    public AccountI(String firstName, String lastName, String pesel, double income) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.pesel = pesel;
        this.income = income;
    }

    @Override
    public double getBalance(Current current) {
        return balance;
    }

    @Override
    public void depositMoney(double amount, Current current) {
        balance+=amount;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
