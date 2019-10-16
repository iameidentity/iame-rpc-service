# IAME RPC Service

This is a spring boot microservice. It contains API key verification and uses Netflix Zuul to route user requests to the destination nodes. 

## What you’ll need

* JDK 1.8 or later
* Maven 3.2+
* MySQL 5.5+

## To run the service with shell command

* Create database
  ```
  mysql -umy_db_user -p -e "CREATE DATABASE my_rpc_db";
  ```
  
* Creating API Key table
  ```
  mysql -umy_db_user -p my_rpc_db < src/schema/iame_rpc/api_keys.sql
  ```
  
* Add an API Key
  ```
  mysql -umy_db_user -p -e "INSERT INTO my_rpc_db.api_keys (email, api_key, status) VALUES ('myname@mail.com', 'MYTESTAPIKEY', 'active')";
  ```
  
* Building the project
  ```
  mvn clean package
  ```
  
* Create my-rpc-service.properties file with your configuration
  ```
  # Your server's IP address (not necessary if you are going to proxy request from your web server)
  server.address=127.0.0.1
  
  # Port number where the service is running
  server.port=7000
  
  # Qtum node configuration
  qtum-rpc.url=http://127.0.0.1:3889
  qtum-rpc.user=qtumrpcusername
  qtum-rpc.pass=qtumrpcpassword
  
  # The URI of your geth node
  ethereum.url=http://127.0.0.1:8545
 
  # Database configuration
  database.host=127.0.0.1
  database.port=3306
  database.name=my_rpc_db
  database.user=my_db_user
  database.pass=my_db_password
  ```
  
# To run the service
  ```
  java -jar -Dspring.config.additional-location=my-rpc-service.properties target/iame-rpc-service-1.0.0.jar
  ```
  
# To test the Qtum RPC service
  ```
  curl -v --data-binary '{"method": "getblockchaininfo"}' -H 'content-type: application/json;' "http://127.0.0.1:7000/qtum-rpc?apiKey=MYTESTAPIKEY"
  ```
