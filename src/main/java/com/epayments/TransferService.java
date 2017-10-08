package com.epayments;

import com.epayments.data.Account;
import com.epayments.data.DataStorage;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * TransferOperation service provides abilities to manage balance between the accounts.
 * @author Daria Nikiforova
 */
@Path("/account")
public class TransferService {

    /**
     * Create account operation.
     * @param name account name
     * @param balance initial balance
     * @return id of the created account
     */
    @POST
    @Path("create")
    public int createAccount(@FormParam("name") String name, @FormParam("balance") double balance) {
        return DataStorage.addAccount(name, balance);
    }

    /**
     * Transfer money operation.
     * @param sourceAccount account from which money should be withdrawn
     * @param receiverAccount account to which money should be deposited
     * @param amount sum to send
     * @return response with operation result status
     */
    @POST
    @Path("transfer")
    public Response transferMoney(@FormParam("source") int sourceAccount, @FormParam("receiver") int receiverAccount,
                                  @FormParam("amount") double amount) {
        try {
            DataStorage.recordTransfer(sourceAccount, receiverAccount, amount);
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        } catch(Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
