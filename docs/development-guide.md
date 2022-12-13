# Development Guide

## Installing the packages

**A more elaborate setup guide is available in the [SetupServer.md document](docs/SetupServer.md).**

## Pre-requisites
- Java <u>**8**</u> (⚠️) JDK (Oracle, Coretto, or **Zulu OpenJDK** (preferred for licensing reasons))
- IntelliJ (my preference) or Visual Studio Code with [Corda plugin](https://github.com/corda/vscode-corda).
  IntelliJ run configurations are included with the project.
- Unix users need xterm
- Windows users add the JDK to the environment variable `PATH`

See https://docs.corda.net/getting-set-up.html for more details.

# Usage

## Running tests inside IntelliJ

We recommend editing your IntelliJ preferences so that you use the Gradle runner - this means that the quasar utils
plugin will make sure that some flags (like ``-javaagent`` - see below) are
set for you.

To switch to using the Gradle runner:

* Navigate to ``Build, Execution, Deployment -> Build Tools -> Gradle`` (or search for `Build Tools` and select the one with settings on the right)
    * Windows: this is in "Settings"
    * MacOS: this is in "Preferences"
* Set "Build and run using" to "Gradle (Default)"
* Set "Run tests using:" to "Gradle (Default)"

If you would prefer to use the built in IntelliJ JUnit test runner, you can run ``gradlew installQuasar`` which will
copy your quasar JAR file to the lib directory. You will then need to specify ``-javaagent:lib/quasar.jar``
and set the run directory to the project root directory for each test.

## Running the nodes
1. open a terminal
2. navigate to the project root
3. run `./gradlew deployNodes`
4. wait until your are prompted `BUILD SUCCESSFUL`
5. navigate to `corda/cordformation/build/nodes` and run `./runnodes`
6. three windows will pop up (node A, node B, notary), wait for these to be finished, you will see an interactive shell

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

## Interacting with the nodes

### Shell

When started via the command line, each node will display an interactive shell:

    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.
    
    Tue Nov 06 11:58:13 GMT 2018>>>

You can use this shell to interact with your node. For example, enter `run networkMapSnapshot` to see a list of
the other nodes on the network:

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

You can find out more about the node shell [here](https://docs.corda.net/shell.html).

### Client

`clients/src/main/kotlin/nl/tno/federated/Client.kt` defines a simple command-line client that connects to a node via RPC
and prints a list of the other nodes on the network.

#### Running the client
### Webserver

`clients/src/main/kotlin/nl/tno/federated/webserver/` defines a simple Spring webserver that connects to a node via RPC and
allows you to interact with the node over HTTP.

#### Running the webserver through IntelliJ
Run the `Run x Server`.

#### Via the command line

Run the `runTemplateServer` Gradle task. By default, it connects to the node with RPC address `localhost:10006` with
the username `user1` and the password `test`, and serves the webserver on port `localhost:10050`.

The webserver can also be run by running `gradlew bootJar` and running the resulting client jar file.

#### Interacting with the webserver
Swagger-UI will be available at:

    http://localhost:10050/swagger-ui.html

# Using the api
## New event
```json
{
"fullEvent": "@base <http:\/\/example.com\/base\/> . @prefix pi: <https:\/\/ontology.tno.nl\/logistics\/federated\/PhysicalInfrastructure#> . @prefix classifications: <https:\/\/ontology.tno.nl\/logistics\/federated\/Classifications#> . @prefix dcterms: <http:\/\/purl.org\/dc\/terms\/> . @prefix LogisticsRoles: <https:\/\/ontology.tno.nl\/logistics\/federated\/LogisticsRoles#> . @prefix rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#> . @prefix owl: <http:\/\/www.w3.org\/2002\/07\/owl#> . @prefix Event: <https:\/\/ontology.tno.nl\/logistics\/federated\/Event#> . @prefix ReusableTags: <https:\/\/ontology.tno.nl\/logistics\/federated\/ReusableTags#> . @prefix businessService: <https:\/\/ontology.tno.nl\/logistics\/federated\/BusinessService#> . @prefix DigitalTwin: <https:\/\/ontology.tno.nl\/logistics\/federated\/DigitalTwin#> . @prefix skos: <http:\/\/www.w3.org\/2004\/02\/skos\/core#> . @prefix xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#> . @prefix ex: <http:\/\/example.com\/base#> . @prefix time: <http:\/\/www.w3.org\/2006\/time#> . @prefix dc: <http:\/\/purl.org\/dc\/elements\/1.1\/> . @prefix era: <http:\/\/era.europa.eu\/ns#> .  ex:Event-b550739e-2ac2-4c21-9a56-e74791313375 a Event:Event, owl:NamedIndividual;   rdfs:label \"GateOut test\", \"Planned gate out\";   Event:hasTimestamp \"2019-09-22T06:00:00Z\"^^xsd:dateTime;   Event:hasDateTimeType Event:Planned;   Event:involvesDigitalTwin ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def, ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:involvesBusinessTransaction ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:hasMilestone Event:START;   Event:hasSubmissionTimestamp \"2019-09-17T23:32:07Z\"^^xsd:dateTime .  ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def a DigitalTwin:TransportMeans,     owl:NamedIndividual .  ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a businessService:Consignment,     owl:NamedIndividual;   businessService:consignmentCreationTime \"2021-05-13T21:23:04Z\"^^xsd:dateTime;   businessService:involvedActor ex:LegalPerson-Maersk .  ex:LegalPerson-Maersk a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;   businessService:actorName \"Maersk\" .  ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a DigitalTwin:Equipment, owl:NamedIndividual;   rdfs:label \"MNBU0494490\" .",
"countriesInvolved": [

]
}
```

```json
{
  "fullEvent": "@base <http:\/\/example.com\/base\/> . @prefix pi: <https:\/\/ontology.tno.nl\/logistics\/federated\/PhysicalInfrastructure#> . @prefix classifications: <https:\/\/ontology.tno.nl\/logistics\/federated\/Classifications#> . @prefix dcterms: <http:\/\/purl.org\/dc\/terms\/> . @prefix LogisticsRoles: <https:\/\/ontology.tno.nl\/logistics\/federated\/LogisticsRoles#> . @prefix rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#> . @prefix owl: <http:\/\/www.w3.org\/2002\/07\/owl#> . @prefix Event: <https:\/\/ontology.tno.nl\/logistics\/federated\/Event#> . @prefix ReusableTags: <https:\/\/ontology.tno.nl\/logistics\/federated\/ReusableTags#> . @prefix businessService: <https:\/\/ontology.tno.nl\/logistics\/federated\/BusinessService#> . @prefix DigitalTwin: <https:\/\/ontology.tno.nl\/logistics\/federated\/DigitalTwin#> . @prefix skos: <http:\/\/www.w3.org\/2004\/02\/skos\/core#> . @prefix xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#> . @prefix ex: <http:\/\/example.com\/base#> . @prefix time: <http:\/\/www.w3.org\/2006\/time#> . @prefix dc: <http:\/\/purl.org\/dc\/elements\/1.1\/> . @prefix era: <http:\/\/era.europa.eu\/ns#> .  ex:Event-7f0140f7-1c22-4b68-9bea-25418cd51d18 a Event:Event, owl:NamedIndividual;   rdfs:label \"GateOut test\", \"Planned gate out\";   Event:hasTimestamp \"2019-09-22T06:00:00Z\"^^xsd:dateTime;   Event:hasDateTimeType Event:Planned;   Event:involvesDigitalTwin ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def, ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:involvesBusinessTransaction ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:hasMilestone Event:End;   Event:hasSubmissionTimestamp \"2019-09-17T23:32:07Z\"^^xsd:dateTime .  ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def a DigitalTwin:TransportMeans,     owl:NamedIndividual .  ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a businessService:Consignment,     owl:NamedIndividual;   businessService:consignmentCreationTime \"2021-05-13T21:23:04Z\"^^xsd:dateTime;   businessService:involvedActor ex:LegalPerson-SomeShipper .  ex:LegalPerson-SomeShipper a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;   businessService:actorName \"SomeShipper\" .  ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a DigitalTwin:Equipment, owl:NamedIndividual;   rdfs:label \"ABCDE\" .",
  "countriesInvolved": [
    
  ]
}
```

# Credits

This project was initially created based on the CorDapp template for Kotlin.
## Installing the packages

**A more elaborate setup guide is available in the [SetupServer.md document](docs/SetupServer.md).**

## Pre-requisites
- Java <u>**8**</u> (⚠️) JDK (Oracle, Coretto, or **Zulu OpenJDK** (preferred for licensing reasons))
- IntelliJ (my preference) or Visual Studio Code with [Corda plugin](https://github.com/corda/vscode-corda).
  IntelliJ run configurations are included with the project.
- Unix users need xterm
- Windows users add the JDK to the environment variable `PATH`

See https://docs.corda.net/getting-set-up.html for more details.

# Usage

## Running tests inside IntelliJ

We recommend editing your IntelliJ preferences so that you use the Gradle runner - this means that the quasar utils
plugin will make sure that some flags (like ``-javaagent`` - see below) are
set for you.

To switch to using the Gradle runner:

* Navigate to ``Build, Execution, Deployment -> Build Tools -> Gradle`` (or search for `Build Tools` and select the one with settings on the right)
    * Windows: this is in "Settings"
    * MacOS: this is in "Preferences"
* Set "Build and run using" to "Gradle (Default)"
* Set "Run tests using:" to "Gradle (Default)"

If you would prefer to use the built in IntelliJ JUnit test runner, you can run ``gradlew installQuasar`` which will
copy your quasar JAR file to the lib directory. You will then need to specify ``-javaagent:lib/quasar.jar``
and set the run directory to the project root directory for each test.

## Running the nodes
1. open a terminal
2. navigate to the project root
3. run `./gradlew deployNodes`
4. wait until your are prompted `BUILD SUCCESSFUL`
5. navigate to `corda/cordformation/build/nodes` and run `./runnodes`
6. three windows will pop up (node A, node B, notary), wait for these to be finished, you will see an interactive shell

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

## Interacting with the nodes

### Shell

When started via the command line, each node will display an interactive shell:

    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.
    
    Tue Nov 06 11:58:13 GMT 2018>>>

You can use this shell to interact with your node. For example, enter `run networkMapSnapshot` to see a list of
the other nodes on the network:

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

You can find out more about the node shell [here](https://docs.corda.net/shell.html).

### Client

`clients/src/main/kotlin/nl/tno/federated/Client.kt` defines a simple command-line client that connects to a node via RPC
and prints a list of the other nodes on the network.

#### Running the client
### Webserver

`clients/src/main/kotlin/nl/tno/federated/webserver/` defines a simple Spring webserver that connects to a node via RPC and
allows you to interact with the node over HTTP.

#### Running the webserver through IntelliJ
Run the `Run x Server`.

#### Via the command line

Run the `runTemplateServer` Gradle task. By default, it connects to the node with RPC address `localhost:10006` with
the username `user1` and the password `test`, and serves the webserver on port `localhost:10050`.

The webserver can also be run by running `gradlew bootJar` and running the resulting client jar file.

#### Interacting with the webserver
Swagger-UI will be available at:

    http://localhost:10050/swagger-ui.html

# Using the api
## New event
```json
{
"fullEvent": "@base <http:\/\/example.com\/base\/> . @prefix pi: <https:\/\/ontology.tno.nl\/logistics\/federated\/PhysicalInfrastructure#> . @prefix classifications: <https:\/\/ontology.tno.nl\/logistics\/federated\/Classifications#> . @prefix dcterms: <http:\/\/purl.org\/dc\/terms\/> . @prefix LogisticsRoles: <https:\/\/ontology.tno.nl\/logistics\/federated\/LogisticsRoles#> . @prefix rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#> . @prefix owl: <http:\/\/www.w3.org\/2002\/07\/owl#> . @prefix Event: <https:\/\/ontology.tno.nl\/logistics\/federated\/Event#> . @prefix ReusableTags: <https:\/\/ontology.tno.nl\/logistics\/federated\/ReusableTags#> . @prefix businessService: <https:\/\/ontology.tno.nl\/logistics\/federated\/BusinessService#> . @prefix DigitalTwin: <https:\/\/ontology.tno.nl\/logistics\/federated\/DigitalTwin#> . @prefix skos: <http:\/\/www.w3.org\/2004\/02\/skos\/core#> . @prefix xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#> . @prefix ex: <http:\/\/example.com\/base#> . @prefix time: <http:\/\/www.w3.org\/2006\/time#> . @prefix dc: <http:\/\/purl.org\/dc\/elements\/1.1\/> . @prefix era: <http:\/\/era.europa.eu\/ns#> .  ex:Event-b550739e-2ac2-4c21-9a56-e74791313375 a Event:Event, owl:NamedIndividual;   rdfs:label \"GateOut test\", \"Planned gate out\";   Event:hasTimestamp \"2019-09-22T06:00:00Z\"^^xsd:dateTime;   Event:hasDateTimeType Event:Planned;   Event:involvesDigitalTwin ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def, ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:involvesBusinessTransaction ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:hasMilestone Event:START;   Event:hasSubmissionTimestamp \"2019-09-17T23:32:07Z\"^^xsd:dateTime .  ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def a DigitalTwin:TransportMeans,     owl:NamedIndividual .  ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a businessService:Consignment,     owl:NamedIndividual;   businessService:consignmentCreationTime \"2021-05-13T21:23:04Z\"^^xsd:dateTime;   businessService:involvedActor ex:LegalPerson-Maersk .  ex:LegalPerson-Maersk a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;   businessService:actorName \"Maersk\" .  ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a DigitalTwin:Equipment, owl:NamedIndividual;   rdfs:label \"MNBU0494490\" .",
"countriesInvolved": [

]
}
```

```json
{
  "fullEvent": "@base <http:\/\/example.com\/base\/> . @prefix pi: <https:\/\/ontology.tno.nl\/logistics\/federated\/PhysicalInfrastructure#> . @prefix classifications: <https:\/\/ontology.tno.nl\/logistics\/federated\/Classifications#> . @prefix dcterms: <http:\/\/purl.org\/dc\/terms\/> . @prefix LogisticsRoles: <https:\/\/ontology.tno.nl\/logistics\/federated\/LogisticsRoles#> . @prefix rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#> . @prefix owl: <http:\/\/www.w3.org\/2002\/07\/owl#> . @prefix Event: <https:\/\/ontology.tno.nl\/logistics\/federated\/Event#> . @prefix ReusableTags: <https:\/\/ontology.tno.nl\/logistics\/federated\/ReusableTags#> . @prefix businessService: <https:\/\/ontology.tno.nl\/logistics\/federated\/BusinessService#> . @prefix DigitalTwin: <https:\/\/ontology.tno.nl\/logistics\/federated\/DigitalTwin#> . @prefix skos: <http:\/\/www.w3.org\/2004\/02\/skos\/core#> . @prefix xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#> . @prefix ex: <http:\/\/example.com\/base#> . @prefix time: <http:\/\/www.w3.org\/2006\/time#> . @prefix dc: <http:\/\/purl.org\/dc\/elements\/1.1\/> . @prefix era: <http:\/\/era.europa.eu\/ns#> .  ex:Event-7f0140f7-1c22-4b68-9bea-25418cd51d18 a Event:Event, owl:NamedIndividual;   rdfs:label \"GateOut test\", \"Planned gate out\";   Event:hasTimestamp \"2019-09-22T06:00:00Z\"^^xsd:dateTime;   Event:hasDateTimeType Event:Planned;   Event:involvesDigitalTwin ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def, ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:involvesBusinessTransaction ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:hasMilestone Event:End;   Event:hasSubmissionTimestamp \"2019-09-17T23:32:07Z\"^^xsd:dateTime .  ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def a DigitalTwin:TransportMeans,     owl:NamedIndividual .  ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a businessService:Consignment,     owl:NamedIndividual;   businessService:consignmentCreationTime \"2021-05-13T21:23:04Z\"^^xsd:dateTime;   businessService:involvedActor ex:LegalPerson-SomeShipper .  ex:LegalPerson-SomeShipper a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;   businessService:actorName \"SomeShipper\" .  ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a DigitalTwin:Equipment, owl:NamedIndividual;   rdfs:label \"ABCDE\" .",
  "countriesInvolved": [
    
  ]
}
```

# Credits

This project was initially created based on the CorDapp template for Kotlin.