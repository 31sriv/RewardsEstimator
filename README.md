## RewardsEstimator
A Spring Boot REST API for calculating and retrieving rewards points based on customer id and transaction over the past three months

#A retailer offers a rewards program to its customers, awarding points based on each recorded purchase. A customer receives 2 points for every dollar spent over $100 in each transaction, plus 1 point for every dollar spent over $50 in each transaction (e.g. a $120 purchase = 2x$20 + 1x$50 = 90 points). Given a record of every transaction during a three month period, calculate the reward points earned for each customer per month and total.

- The package name is structured as com.retailer.rewards
- Used MySQL database to store the information
- Do run the script.sql on MySQL to prepare the test data
- In sql script, the DB consists of two tables , Customers and Transactions for maintaining record of customers and storing the Transaction details of each customer.
- Install MySQL db locally and run it. Change the db settings in application.properties file.
- Used Mockito , Junit library for unit testing
- Please refer DatabaseSetupFile.pdf to configure the MySQL db settings in the Project.

 [https://github.com/31sriv/RewardsEstimator/blob/main/DatabaseSetupFile.pdf](https://github.com/31sriv/RewardsEstimator/blob/main/DatabaseSetupFile.pdf)


**Technologies Used**
- Java 8+
- Spring Boot 3.2
- MySQL
- Maven

**Tools Used**
- Intellij Ide Ultimate - for development of the project
- PostMan - for API testing

**How to run the app**<br>
Navigate to the project folder using your terminal or cmd , then run the following command 

   `mvn spring-boot:run`


**API Endpoint**<br> 
Get Reward Points by Customer ID

**GET** `/customers/{customerId}/rewards`

**Response Example**
```json
{
    "customerName": "Kriti Sen",
    "monthlyRewards": [
        {
            "month": "April",
            "totalPoints": 120,
            "transactions": [
                {
                    "transactionId": 10003,
                    "amount": 135,
                    "rewardPoints": 120
                }
            ]
        },
        {
            "month": "May",
            "totalPoints": 100360,
            "transactions": [
                {
                    "transactionId": 10002,
                    "amount": 190,
                    "rewardPoints": 230
                },
                {
                    "transactionId": 10008,
                    "amount": 50,
                    "rewardPoints": 0
                },
                {
                    "transactionId": 10009,
                    "amount": 50000,
                    "rewardPoints": 99850
                },
                {
                    "transactionId": 10005,
                    "amount": 170,
                    "rewardPoints": 190
                },
                {
                    "transactionId": 10011,
                    "amount": 120,
                    "rewardPoints": 90
                }
            ]
        },
        {
            "month": "June",
            "totalPoints": 470,
            "transactions": [
                {
                    "transactionId": 10001,
                    "amount": 90,
                    "rewardPoints": 40
                },
                {
                    "transactionId": 10010,
                    "amount": 0,
                    "rewardPoints": 0
                },
                {
                    "transactionId": 10004,
                    "amount": 290,
                    "rewardPoints": 430
                }
            ]
        }
    ],
    "totalRewardPoints": 100950
}
```

 GET `http://localhost:8080/customers/{customerId}/rewards`
