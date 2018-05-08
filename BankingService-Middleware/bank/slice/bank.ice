#ifndef BANK_ICE
#define BANK_ICE

module Bank
{
    enum AccountTypes {STANDARD, PREMIUM};
    enum CurrencyType
    {
        PLN = 0,
        CHF = 1,
        EUR = 2,
        USD = 3
    };

    exception DateError
    {
        string reason;
    };
    exception InvalidValueError
    {
        string reason;
    };

    class Credit
    {
        CurrencyType foreignCurrency;
        double foreignCost;
        CurrencyType domesticCurrency;
        double domesticCost;
    };


    interface Account
    {
         double getBalance();
         void depositMoney(double amount);
    };
    interface PremiumAccount extends Account
    {
         Credit getCredit(double amount, CurrencyType currency, long startDate, long endDate) throws DateError, InvalidValueError;

    };
    interface AccountFactory
    {
        Account * createAccount(string firstName, string lastName, string pesel, double monthlyIncome)throws InvalidValueError;
    };

};

#endif
