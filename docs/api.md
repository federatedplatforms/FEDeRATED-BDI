# BDI API

BDI API exposed a few endpoints for interaction with a BDI Node.

## Available endpoints

| Endpoint                                                                                                             | Description                                          |
|----------------------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| [/events](../api/src/main/kotlin/nl/tno/federated/api/controllers/EventController.kt)                                | Event related endpoints                              | 
| [/datapull](../api/src/main/kotlin/nl/tno/federated/api/controllers/DataPullController.kt)                           | Data pull related endpoints                          |
| [/node](../api/src/main/kotlin/nl/tno/federated/api/controllers/NodeController.kt)                                   | Corda Node related endpoints                         |
| [/tradelens](../semantic-adapter/src/main/kotlin/nl/tno/federated/semantic/adapter/tradelens/TradelensController.kt) | Tradelens specific endpoints of the semantic adapter |

## Build

This project can be build using Gradle. 

```
.\gradlew build --info
```

## How to run

This project can be run using Gradle.

```
.\gradlew bootRun --info
```

## Swagger UI

Once the BDI API application is started, the following endpoint will be availble for accessing the Swagger API documentation:

- http://localhost:10500/swagger-ui.html

## Frameworks and libraries
 
### Kotlin

Code was written in Kotlin and is compatible with Kotlin 1.3

### Spring Boot

The BDI application was build using Spring Boot 2.3.12. 

### CordaRPC

Connection to Corda Nodes is made using the CordaRPC library. 

### Semantic Adapter

Semantic Adapter module is included in the BDI API application, therefore the Semantic Adapter endpoints are also exposed via the BDI API.

## Configuration

The [application.properties](../api/src/main/resources/application.properties) contains all the configuration properties for the BDI API application.