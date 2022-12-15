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
When starting the Docker container for GraphDB two repositories will automatically be intialized, the `bdi` and `private` repositories.
Next to that the `bdi` repository is initialized with the ontologies and federated SHACL files.

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

### Interacting using the BDI API 

The API also provides a way of interacting with Corda nodes via HTTP. Please refer to the [api](api.md) docs on how to work with the BDI API. 


# Credits

This project was initially created based on the CorDapp template for Kotlin.
