# Development Guide

This document describes how to setup a development environment for this repository. 

## Pre-requisites

### Hardware

For development purposes it's recommended to use a machine with x86 architecture and to have at least 16GB of memory.    

### JDK

This project uses Java 8 (⚠ Corda 4.9 only supports Java 8). The code has been build and tested using Zulu OpenJDK. 

### Corda setup

For more information regarding the setup of Corda development environment please refer to: https://docs.r3.com/en/platform/corda/4.9/community/getting-set-up.html for more details.

### Gradle

Gradle 6.3 is used as the build system. Gradle will automatically be downloaded once a command is issued using the gradle wrapper (gradlew). 
Upgrading to a newer Gradle version will result in compatibility issues with the Corda gradle plugins.

### IDE 

For development JetBrains IntelliJ is recommended as this repository provides run configurations and an editorconfig.

### GraphDB

This project uses GraphDB free version 10 as a triple store. It's not required to download, install and setup manually as a Docker composed file is available for running GraphDB.

### Docker compose

Make sure Docker desktop is installed, Docker is used for automated containerized testing of GraphDB during the build/tests.

## Building the project

This project can be build using Gradle by issuing the following command from the project root directory:

```
./gradlew build --info
```

Building the application will also trigger (unit/integration) test to run.

## Running GraphDB

GraphDB is used in the Corda workflows a persistent triple store. This project includes a docker-compose.yml file for running GraphDB. 
When starting the Docker container for GraphDB two repositories will automatically be intialized, the `federated` repository.

## Running the Corda nodes

The [corda/cordformation](corda/cordformation) module contains a preconfigured corda node network consisting
of 3 nodes and 1 notary. In order to run the corda nodes the cordformation build task needs to be 
executed first. This can be done by executing the following command from the project root directory:

```
./gradlew deployNodes --info
```

Wait until your are prompted `BUILD SUCCESSFUL`. Then navigate to `corda/cordformation/build/nodes` and run `./runnodes`.
Four windows will pop up (node A, node B, node C, notary), wait for these to be finished, you will see an interactive shell

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

## Interacting with the nodes

Before interacting with any of the nodes, make sure that GraphDB has been started!

### Shell

Corda provides an interactive shell. After each node was started via the command line, nodes will display an interactive shell:
```
    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.
    
    Tue Nov 06 11:58:13 GMT 2018>>>
```

You can use this shell to interact with your node. For example, enter `run networkMapSnapshot` to see a list of
the other nodes on the network:

```
    Tue Nov 06 11:58:13 GMT 2018>>> run networkMapSnapshot
    [
      {
      "addresses" : [ "localhost:10002" ],
      "legalIdentitiesAndCerts" : [ "O=Notary, L=London, C=GB" ],
      "platformVersion" : 3,
      "serial" : 1541505484825
    },
      {
      "addresses" : [ "localhost:10005" ],
      "legalIdentitiesAndCerts" : [ "O=PartyA, L=London, C=GB" ],
      "platformVersion" : 3,
      "serial" : 1541505382560
    },
      {
      "addresses" : [ "localhost:10008" ],
      "legalIdentitiesAndCerts" : [ "O=PartyB, L=New York, C=US" ],
      "platformVersion" : 3,
      "serial" : 1541505384742
    }
    ]
    
    Tue Nov 06 12:30:11 GMT 2018>>> 
```

You can find out more about the node shell [here](https://docs.corda.net/shell.html).

### Interacting using the FEDeRATED Node API 

The API also provides a way of interacting with Corda nodes via HTTP. Please refer to the [api](api.md) docs on how to work with the FEDeRATED Node API. 

## Configuration

[application.properties](../api/src/main/resources/application.properties) contains all the configuration properties for the FEDeRATED Node API application. All the properties can be overridden by passing them to the startup command. For more information see: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.files

Important properties are listed in the table below.

| Endpoint                          | Description                               | Default value                                |
|-----------------------------------|-------------------------------------------|----------------------------------------------|
| federated.node.corda.rpc.host     | Corda RPC hostname                        | localhost                                    |
| federated.node.corda.rpc.port     | Corda RPC port                            | 10006                                        |
| federated.node.corda.rpc.username | Corda RPC username                        | user1                                        |
| federated.node.corda.rpc.password | Corda RPC password                        | vzzuABeCut3jGoJfEp94                         |
| federated.node.graphdb.sparql.url | GraphDB repository SPARQL endpoint URL    | http://localhost:7200/repositories/federated | 
| server.port                       | FEDeRATED Node API server port            | 10050                                        | 


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

The FEDeRATED Node application can be started without any corda node being up and running.
In order to be able to successfully test the integration with Corda you need to run one or more corda nodes.
See the CordaRPC section for more details/

## Frameworks and libraries

### Kotlin

Code was written in Kotlin and is compatible with Kotlin 1.7/1.3

### Spring Boot

The FEDeRATED Node application was build using Spring Boot 3.2.*. For more information regarding Spring Boot please refer to the Spring Boot documentation:

- https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/

### CordaRPC

Connection to Corda nodes is done using the CordaRPC library. In this prototype we are using the default settings, but CordaRPC can also use TLS.
For more information regarding the CordaRPC please refer to the official corda documentation:

- https://docs.r3.com/en/platform/corda/4.9/community/clientrpc.html

Configuration details for the CordaRPC connection are described in the configuration section below.


# Credits

This project was initially created based on the CorDapp template for Kotlin.
