# Development Guide

## Pre-requisites

### JDK

This project uses Java <u>**8**</u>⚠️ due to Corda only supporting Java 8 at the time of writing. It was tested using Zulu OpenJDK (preferred for licensing reasons).

### Gradle

Gradle 6.3 is used as the build system. Gradle will automatically be downloaded once a command is issued using the gradle wrapper (gradlew).

### IDE 

Development was done using JetBrains IntelliJ, run configurations and editorconfig are included with the project.

### Corda setup

For more information regarding the setup of Corda development environment please refer to: https://docs.corda.net/getting-set-up.html for more details.

### GraphDB

This project uses GraphDB free version 10 for its triple store. Its not required to download and install is manually as a Docker composed file is available for running GraphDB.

### Docker compose

Make sure Docker desktop is installed, Docker is used for automated containerized testing of GraphDB during the build/tests.

## Building the project

This project can be build using Gradle by issuing the following command from the project root directory:

```
./gradlew build --info
```

Building the application will also trigger (unit/integration) test to run.

## Running GraphDB

GraphDB is used in the Corda workflows a persistent triple store. 
This project includes a docker-compose.yml file for running GraphDB. 
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
