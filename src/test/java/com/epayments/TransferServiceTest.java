package com.epayments;

import com.epayments.data.Account;
import com.epayments.data.DataStorage;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the TransferService functionality.
 * @author Daria Nikiforova
 */
public class TransferServiceTest {

    private TransferService transferService = new TransferService();

    @Before
    public void setUp() {
        DataStorage.clearDataStorage();
    }

    @Test
    public void createAndGetAnAccount() {
        int id1 = transferService.createAccount("user1", 1700.0);
        assertEquals(0, id1);
        int id2 = transferService.createAccount("user2", 2000.0);
        assertEquals(1, id2);
        Account account1 = DataStorage.getAccount(0);
        Account account2 = DataStorage.getAccount(1);

        assertEquals("user1", account1.getName());
        assertEquals("user2", account2.getName());
    }

    @Test
    public void transferMoneySuccessfully() {
        transferService.createAccount("user1", 1700.0);
        transferService.createAccount("user2", 2000.0);

        Response response = transferService.transferMoney(0, 1, 200.0);
        assertEquals(200, response.getStatus());
        Account account1 = DataStorage.getAccount(0);
        Account account2 = DataStorage.getAccount(1);;
        assertEquals(1500.0, account1.getBalance(), 0.001);
        assertEquals(2200.0, account2.getBalance(), 0.001);
    }

    @Test
    public void transferMoneyExceedingBalance() {
        transferService.createAccount("user1", 1700.0);
        transferService.createAccount("user2", 2000.0);

        Response response = transferService.transferMoney(1, 0, 3000.0);
        assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        Account account1 = DataStorage.getAccount(0);
        Account account2 = DataStorage.getAccount(1);
        assertEquals(1700.0, account1.getBalance(), 0.001);
        assertEquals(2000.0, account2.getBalance(), 0.001);
    }


}
