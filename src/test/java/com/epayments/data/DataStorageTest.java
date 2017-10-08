package com.epayments.data;

import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests for the DataStorage functionality.
 * @author Daria Nikiforova
 */
public class DataStorageTest {

    private List<Account> expectedAccounts = Arrays.asList(
        new Account(0, "anna", 1000.0),
        new Account(1, "john", 2000.0),
        new Account(2, "tom", 1500.0),
        new Account(3, "alex", 2350.0),
        new Account(4, "kate", 4000.0),
        new Account(5, "denis", 2300.0),
        new Account(6, "helen", 4000.0),
        new Account(7, "john", 2300.0)
    );

    @Before
    public void setUp() {
        DataStorage.clearDataStorage();
    }

    private void createAccounts() {
        expectedAccounts.forEach(account -> {
            DataStorage.addAccount(account.getName(), account.getBalance());
        });
    }

    @Test
    public void createAccountThreadSafely() {
        List<Thread> threads = Arrays.asList(
                new Thread(() -> {
                    DataStorage.addAccount("anna", 1000.0);
                    DataStorage.addAccount("john", 2000.0);
                }),
                new Thread(() -> {
                    DataStorage.addAccount("tom", 1500.0);
                    DataStorage.addAccount("alex", 2350.0);
                }),
                new Thread(() -> {
                    DataStorage.addAccount("kate", 4000.0);
                    DataStorage.addAccount("denis", 2300.0);
                }),
                new Thread(() -> {
                    DataStorage.addAccount("helen", 4000.0);
                    DataStorage.addAccount("john", 2300.0);
                }));

        threads.forEach(Thread::start);

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail("The exception was occured");
            }
        });

        List<Account> accounts = DataStorage.getAllAccounts();
        assertEquals(expectedAccounts.size(), accounts.size());

        Set<Integer> ids = accounts.stream().map(Account::getId).collect(Collectors.toSet());
        assertEquals(expectedAccounts.size(), ids.size());

        expectedAccounts.forEach(account ->
            assertTrue(accounts.stream().anyMatch(ac -> ac.getName().equals(account.getName())
                        && ac.getBalance() == account.getBalance())));
    }

    @Test
    public void sendMoneyToTheAccountMakingTransfer() {
        createAccounts();
        List<Thread> threads = Arrays.asList(
            new Thread(() -> {
                DataStorage.recordTransfer(0, 7, 250.0);
            }),
            new Thread(() -> {
                DataStorage.recordTransfer(1, 7, 200.0);
            }),
            new Thread(() -> {
                DataStorage.recordTransfer(7, 5, 300.0);
            }));

        threads.forEach(Thread::start);

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail("The exception was occured");
            }
        });

        Account account1 = DataStorage.getAccount(1);
        Account account7 = DataStorage.getAccount(7);
        Account account5 = DataStorage.getAccount(5);
        Account account0 = DataStorage.getAccount(0);

        assertEquals(expectedAccounts.get(1).getBalance() - 200.0, account1.getBalance(), 0.001);
        assertEquals(expectedAccounts.get(7).getBalance() + 200.0 + 250.0 - 300.0, account7.getBalance(), 0.001);
        assertEquals(expectedAccounts.get(5).getBalance() + 300.0, account5.getBalance(), 0.001);
        assertEquals(expectedAccounts.get(0).getBalance() - 250.0, account0.getBalance(), 0.001);
    }

    @Test
    public void sendMoneyWithDeadlockPossibility() {
        createAccounts();
        List<Thread> threads = Arrays.asList(
                new Thread(() -> {
                    DataStorage.recordTransfer(0, 7, 250.0);
                }),
                new Thread(() -> {
                    DataStorage.recordTransfer(2, 7, 200.0);
                }),
                new Thread(() -> {
                    DataStorage.recordTransfer(7, 0, 300.0);
                }));

        threads.forEach(Thread::start);

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail("The exception was occured");
            }
        });

        Account account7 = DataStorage.getAccount(7);
        Account account2 = DataStorage.getAccount(2);
        Account account0 = DataStorage.getAccount(0);

        assertEquals(expectedAccounts.get(7).getBalance() + 250.0 + 200.0 - 300.0, account7.getBalance(), 0.001);
        assertEquals(expectedAccounts.get(2).getBalance() - 200.0, account2.getBalance(), 0.001);
        assertEquals(expectedAccounts.get(0).getBalance() - 250.0 + 300.0, account0.getBalance(), 0.001);
    }
}
