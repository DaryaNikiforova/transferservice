package com.epayments.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data storage provides API for account management - creation and money sending.
 * @author Daria Nikiforova
 */
public final class DataStorage {
    /**
     * Accounts storage.
     */
    private static ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<>();
    /**
     * Sequence of account ids which should be unique.
     */
    private static AtomicInteger accountId = new AtomicInteger();

    /**
     * Creates the account entity and saves it in the storage.
     * @param name account name
     * @param balance account initial balance
     * @return id of the created account
     */
    public static int addAccount(String name, double balance) {
        Account account = new Account(accountId.getAndIncrement(), name, balance);
        accounts.putIfAbsent(account.getId(), account);
        return account.getId();
    }

    /**
     * Records the changes after money transferring for source account and receiver accout.
     * This operation is synchronised.
     * @param sourceId id of the source account
     * @param receiverId id of the receiver account
     * @param amount money that should be transferred
     */
    public static void recordTransfer(int sourceId, int receiverId, double amount) {
        //Locks declaring to be able to synchronize operations with accounts money
        Account lock1;
        Account lock2;
        //Get source and receiver accounts
        Account sourceAccount = accounts.get(sourceId);
        Account receiverAccount = accounts.get(receiverId);
        //The order of objects synchronization is important here in order to avoid deadlock issue.
        //So we compare ids to define it and prevent possible deadlocks.
        if (sourceId > receiverId) {
            lock1 = sourceAccount;
            lock2 = receiverAccount;
        } else {
            lock1 = receiverAccount;
            lock2 = sourceAccount;
        }
        //synchronization of withdraw and deposit operation for source and receiver objects
        synchronized (lock1) {
            synchronized (lock2) {
                if (sourceAccount.getBalance() < amount) {
                    throw new IllegalArgumentException("Insufficient funds on your account.");
                }
                withdraw(sourceAccount, amount);
                deposit(receiverAccount, amount);
            }
        }
    }

    /**
     * Gets account by the id.
     * @param id account id
     * @return account entity
     */
    public static Account getAccount(int id) {
        Account result = accounts.get(id);
        if (result == null) {
            throw new IllegalArgumentException("There is no account with id = " + id);
        }
        return result;
    }

    /**
     * Gets the list of all accounts.
     * @return list with all accounts
     */
    public static List<Account> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }

    /**
     * Deposit money to the account.
     * @param account account to which money will be deposited
     * @param amount a sum to deposit
     */
    private static void deposit(Account account, double amount) {
        Account receiver = accounts.get(account.getId());
        receiver.setBalance(account.getBalance() + amount);
        accounts.replace(account.getId(), receiver);
    }

    /**
     * Withdraw money from the account.
     * @param account account from which money will be withdrawn
     * @param amount a sum to withdraw
     */
    private static void withdraw(Account account, double amount) {
        Account source = accounts.get(account.getId());
        source.setBalance(account.getBalance() - amount);
        accounts.replace(account.getId(), source);
    }

    /**
     * Clear storage. Is needed for testing.
     */
    public static void clearDataStorage() {
        accounts = new ConcurrentHashMap<>();
        accountId = new AtomicInteger();
    }
}
