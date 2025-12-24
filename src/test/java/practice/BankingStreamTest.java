package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BankingStreamTest {

    public record Account(Long id, String accountNumber, String owner) {}
    public record Transaction(Long id, String accountNumber, double amount, String type) {} // type: "DEPOSIT" or "WITHDRAW"

    private final List<Account> accounts = List.of(
            new Account(1L, "ACC100", "Milan"),
            new Account(2L, "ACC101", "Ana"),
            new Account(3L, "ACC102", "Nikola")
    );

    private final List<Transaction> transactions = List.of(
            new Transaction(1L, "ACC100", 500, "DEPOSIT"),
            new Transaction(2L, "ACC100", 200, "WITHDRAW"),
            new Transaction(3L, "ACC101", 1000, "DEPOSIT"),
            new Transaction(4L, "ACC102", 300, "DEPOSIT"),
            new Transaction(5L, "ACC102", 100, "WITHDRAW")
    );

    @Test
    void shouldCalculateBalancePerAccount() {
        Map<String, Double> balancePerAccount = transactions.stream()
                .collect(groupingBy(Transaction::accountNumber,
                        summingDouble(t -> t.type().equals("DEPOSIT") ? t.amount() : -t.amount())));

        assertEquals(300, balancePerAccount.get("ACC100"));
        assertEquals(1000, balancePerAccount.get("ACC101"));
        assertEquals(200, balancePerAccount.get("ACC102"));
    }

    @Test
    void shouldFindAccountsWithBalanceAbove500() {
        Map<String, Double> balancePerAccount = transactions.stream()
                .collect(groupingBy(Transaction::accountNumber,
                        summingDouble(t -> t.type().equals("DEPOSIT") ? t.amount() : -t.amount())));

        List<String> richAccounts = balancePerAccount.entrySet().stream()
                .filter(e -> e.getValue() > 500)
                .map(Map.Entry::getKey)
                .toList();

        assertEquals(List.of("ACC101"), richAccounts);
    }

    @Test
    void shouldGetTotalDeposits() {
        double totalDeposits = transactions.stream()
                .filter(t -> t.type().equals("DEPOSIT"))
                .mapToDouble(Transaction::amount)
                .sum();

        assertEquals(1800, totalDeposits);
    }

    @Test
    void shouldGetTopTransactionPerAccount() {
        Map<String, Transaction> topTransaction = transactions.stream()
                .collect(groupingBy(Transaction::accountNumber,
                        collectingAndThen(maxBy((t1, t2) -> Double.compare(t1.amount(), t2.amount())),
                                opt -> opt.orElseThrow())));

        assertEquals(500, topTransaction.get("ACC100").amount());
        assertEquals(1000, topTransaction.get("ACC101").amount());
        assertEquals(300, topTransaction.get("ACC102").amount());
    }

    @Test
    void shouldGetAccountsWithNoWithdrawals() {
        List<String> noWithdrawals = accounts.stream()
                .map(Account::accountNumber)
                .filter(accNum -> transactions.stream()
                        .noneMatch(t -> t.accountNumber().equals(accNum) && t.type().equals("WITHDRAW")))
                .toList();

        assertEquals(List.of("ACC101"), noWithdrawals);
    }

    @Test
    void shouldGroupTransactionsByType() {
        Map<String, List<Transaction>> byType = transactions.stream()
                .collect(groupingBy(Transaction::type));

        assertEquals(3, byType.get("DEPOSIT").size());
        assertEquals(2, byType.get("WITHDRAW").size());
    }

    @Test
    void shouldCalculateTotalBalanceAllAccounts() {
        double totalBalance = transactions.stream()
                .mapToDouble(t -> t.type().equals("DEPOSIT") ? t.amount() : -t.amount())
                .sum();

        assertEquals(1500, totalBalance);
    }

    @Test
    void shouldFindAccountWithHighestBalance() {
        Map<String, Double> balancePerAccount = transactions.stream()
                .collect(groupingBy(Transaction::accountNumber,
                        summingDouble(t -> t.type().equals("DEPOSIT") ? t.amount() : -t.amount())));

        String richest = balancePerAccount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow();

        assertEquals("ACC101", richest);
    }

    @Test
    void shouldGetAllTransactionAmountsSortedDescending() {
        List<Double> amountsDesc = transactions.stream()
                .map(Transaction::amount)
                .sorted((a, b) -> Double.compare(b, a))
                .toList();

        assertEquals(List.of(1000.0, 500.0, 300.0, 200.0, 100.0), amountsDesc);
    }
}