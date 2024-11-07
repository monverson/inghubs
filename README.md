# Brokage Firm Challenge
This project is a Spring Boot application packaged as an executable JAR file. The project uses Java 21, Spring Boot and can be compiled with Maven.

## How to build and run locally
### Run the application locally
1. Clone the project and import it as Maven project in your IDE.
2. Open a terminal in your project root folder and change to the directory as a `inghubs` folder and run `mvn clean install` from the console
3. After that, run the main class `Application.java` than the application will start running on port 8080.

### Database
* This application uses H2 memory database.You lost all data in db after stop the application.
* After starting the application, we can navigate to http://localhost:8080/h2-console, which will present us with a login page.On the login page, we’ll supply the same credentials that we used in the application.properties:
  Once we connect, we’ll see a comprehensive webpage that lists all the tables on the left side of the page and a textbox for running SQL queries:

## REST API

You need to be registered to be able to send requests to all api's, otherwise you will get a 403 error. A jwt token is created for all customers (customer specific). These tokens are kept in the database. If there is no customer information available in the database, it is not possible to generate a jwt token. Therefore, it is first necessary to register using the register endpoint. 

`POST localhost:8080/auth/register`

 request body example for registration;

   ````json
    {
    "userName" : "yusuf",
    "password" : "1234",
    "role" : "ADMIN"
    }
   ````
response example: 

  ````json
    {
  "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlcyI6IkFETUlOIiwic3ViIjoibW9udmVyc29uMSIsImlhdCI6MTczMDkxOTQ5NSwiZXhwIjoxNzMxMDA1ODk1fQ.JQ_C8IEi_Royr0yu5bx5OzrfklSX8Efx9zIVOQmkrJQ"
    }
   ````

Once registered, it is possible to generate new jwt tokens with the login endpoint using the same information.Once we have the jwt token, we can send requests to all endpoints.

To create order:

`POST localhost:8080/orders`

request body example for creating order;

  ````json
   {
  "customer" : {
    "id" : 3,
    "username" : "admin",
    "password" : "password"
  },
  "assetName" : "Gold",
  "orderSide" : "BUY",
  "size" : 10,
  "price" : 100
}
   ````

After selecting authentication type barear token in postman, we can write the generated jwt token there and send a request.

To delete order: 

`DELETE localhost:8080/orders/1`

To matched the order:

`POST localhost:8080/admin/matchedOrder/1`

To top up the deposit:

`POST localhost:8080/transactions/deposit?customerId=3&amount=1000`


To withdraw money:

`POST localhost:8080/transactions/withdraw?customerId=3&amount=1000`

