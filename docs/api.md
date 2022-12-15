# BDI API

BDI API exposed all the endpoints for interaction with a BDI Node.

## Available endpoints

| Endpoint                                                                                                             | Description                                          |
|----------------------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| [/events](../api/src/main/kotlin/nl/tno/federated/api/controllers/EventController.kt)                                | Event related endpoints                              | 
| [/datapull](../api/src/main/kotlin/nl/tno/federated/api/controllers/DataPullController.kt)                           | Data pull related endpoints                          |
| [/node](../api/src/main/kotlin/nl/tno/federated/api/controllers/NodeController.kt)                                   | Corda Node related endpoints                         |
| [/tradelens](../semantic-adapter/src/main/kotlin/nl/tno/federated/semantic/adapter/tradelens/TradelensController.kt) | Tradelens specific endpoints of the semantic adapter |

## API documentation

When the BDI API application is started, the following endpoint will be available for accessing the Swagger API documentation:

- http://localhost:10050/swagger-ui.html

In order to start the application first build and run it.

## Build

This project can be build using Gradle by issuing the following command: 

```
./gradlew build --info
```

Building the application will also trigger (unit/integration) test to run.

## How to run

When the project is successfully build, it can be run using Gradle by issuing the following command:

```
./gradlew bootRun --info
```

When server starts it should display a message in to console:

```
INFO  [main] org.springframework.boot.StartupInfoLogger: Started ServerKt in 1.234 seconds
```

To test the application navigate to the Swagger API documentation page (see API documentation). 

The BDI application can be started without any corda node being up and running. 
In order to be able to successfully test the integration with Corda you need to run one or more corda nodes.
See the CordaRPC section for more details/

## Frameworks and libraries
 
### Kotlin

Code was written in Kotlin and is compatible with Kotlin 1.3

### Spring Boot

The BDI application was build using Spring Boot 2.3.12. For more information regarding Spring Boot please refer to the Spring Boot documentation: 

- https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/

### CordaRPC

Connection to Corda nodes is done using the CordaRPC library. In this prototype we are using the default settings, but CordaRPC can also use TLS. 
For more information regarding the CordaRPC please refer to the official corda documentation:

- https://docs.r3.com/en/platform/corda/4.9/community/clientrpc.html

Configuration details for the CordaRPC connection are described in the configuration section below.

### Semantic Adapter

Semantic Adapter module is included in the BDI API application, therefore the Semantic Adapter endpoints are also exposed via the BDI API.

## Configuration

The [application.properties](../api/src/main/resources/application.properties) contains all the configuration properties for the BDI API application.

Overview of the most important properties:

| Endpoint            | Description         |
|---------------------|---------------------|
| config.rpc.host     | Corda node hostname | 
| config.rpc.port     | Corda node RPC port |
| config.rpc.username | Corda node username |
| config.rpc.password | Corda node password |
| server.port         | BDI API server port |
| tradelens.apikey    | Tradelens API key   |
| tradelens.orgId     | Tradelens Org ID    |
