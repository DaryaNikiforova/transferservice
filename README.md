# transferservice
This application simulates work of the service which provides abilities to transfer money between the accounts to end users.

**Transfer service**

**Used technologies**

The application is built with the following technologies usage:
- Java 8
- Jax-RS library
- JUnit
- Jetty
- Maven

**Solution design**

There is a service class (TransferService.java) which implements two REST operations:

1. createAccount(@FormParam("name") String name, @FormParam("balance") double balance) 
To create monetary account with a name and initial balance

2. transferMoney(@FormParam("source") int sourceAccount, @FormParam("receiver") int receiverAccount,
                                  @FormParam("amount") double amount)
To transfer money between two accounts

At this stage, service layer interacts with the persistance layer. For the sake of implementation simplicity and clarity, 
the data is stored in memory. 
Surely, there will be a lot of simultaneous service calls in real life. 
So the persistance layer should be synchronized in order to provide correct data to a customer.
As the data is stored in the memory, synchronization is made with java.util.concurrent tools usage. 

**How to run**

Open the class AppServer.java from com.epayments package, which starts the Jetty server and run it.
When the server is successfully started you can eaily send HTTP request with any appropriate tool, for example curl:

1. To create the account

curl -d "name=tom&balance=1350.0" http://localhost:8080/account/create

2. To transfer money (500.0) from account with id=0 to account with id=1

curl -d "source=0&receiver=1&amount=500.0" http://localhost:8080/account/transfer
