# Testing

This document describes the test setup.

## Unit testing

The individual modules all contain a set of Unit tests. Unit tests have been written using JUnit. 
The tests are automatically being run when building the project with Gradle.

## Integration testing

[Corda workflows](../corda/workflows) module includes integration tests that use the Corda MockNetwork to verify if
Corda workflows are working as expected. 

## Gitlab CI pipeline

A Gitlab CI pipeline definition is included in the root of the project.

## End-to-end testing of the BDI node

As described in this documentation, the BDI node is not a monolithic application that can be run as a stand-alone element. Instead, it is meant to operate in a network composed of nodes (peers) and at least one notary. The single entity of the BDI node is then in turn formed by a set of components that operate together.

The features of the BDI node can be tested in a local environment where a minimum set of the components needed are running. In particular, it is needed:
  - A network of corda nodes where peers and the notary are known to each other;
  - One entity of GraphDB (every Corda node will see this one instance as its own triple-store database, it is meant only for testing purpose; in a real environment every node would connect to its own GraphDB instance);
  - One running API for each node that requires user interaction;

You can follow the development guide to start-up all these components.  
You can change the identities of the nodes in the file `corda/cordformation/build.gradle` and check which ports to use to connect your API to the node of your choice.

In the default scenario you will have 3 running nodes (in the Netherlands, Germany and Great Britain) and 1 Notary (in Luxemburg).

We propose the following "journey" to test the BDI main functionalities:
  - Data Distribution
    1. The node in GB posts an event that is planned to happen in the NL (for instance an arrival event). The BDI will understand that such an event is meant for the NL and the distribution algorithm will forward it to the node in the NL. The node in DE will have no knowledge of that event.
    2. The node in GB now posts another event that will take place in DE. As in the previous case, nothing will be shared with the node in NL this time.
  - Data Pull
    1. The node in the NL will perform a SPARQL query on its own database to get info about what to ask for details over;
    2. The node in the NL will now query for more detailed data at the node that shared the event related to the details asked;

### Data Distribution - 1

Once the Corda network with the 3 default nodes is up and running, as well as an initialized instance of GraphDB, you can start one API and connect it to the British node (rpc port, `10009`). You can either use a configuration through IntelliJ to run the API from the IDE, or you can build the jar with:

```
./gradlew bootJar
```

and then you can run the jar (that you can find in `api/build/libs`) with:

```
java -jar server.port=10053;config.rpc.host=localhost;config.rpc.port=10009;config.rpc.username=user1;config.rpc.password=vzzuABeCut3jGoJfEp94 api.jar
```

You can interact with the Swagger interface at:

```
http://localhost:10053/swagger-ui.html#/
```

Use the following event data as test:

```ttl
EVENT DATA TO BE DISTRIBUTED
```

This is an event taking place in the NL. The distribution algorithm is currently simple and meant to showcase the possibilities. In this case it will distribute the even to all nodes in a specific country. More complex algorithms can for instance take into account the type of organization running a node (authority vs business) or leverage other data included in the RDF event.

We can upload this event through the endpoint:

```
/events/autodistributed
```

Once the processing is done you can check through the `GET Event` endpoint that the state has been correctly registered in the Corda vault.

You can also start up the API for the node in the NL and the one in DE – mind to adjust the ports in the command you use to run the jar – and verify through the same `GET Event` endpoint that the former will have received the event, while the latter will have an empty vault.

### Data Distribution - 2

You can repeat the previous tryout changing the country and observe that it gets distributed elsewhere.

### Data Pull - 1 & 2

The general assumption when new data is fed into a node is that it comes from an external system that is configured to first translate data from its own model to the BDI ontology, and then it is also supposed to feed a private GraphDB repository with enriched detailed data that are supposed not to be shared with anyone unless an authority asks for it. We are going to test this scenario but first some more set-up is needed.

In the previous tests we (the user) acted as the system feeding data. Now we can suppose that when we shared the first event, we (the system) also uploaded detailed related data in the private repository of GraphDB. We can simulate this by doing it manually:

> INSTRUCTIONS TO MANUALLY INPUT DATA IN GDB PRIVATE REPO

Now, let us use the API connected to the node in the NL, let us assume that we are the Customs and we want to get details over a vessel that arrived at a specific time. We want to get the UUID of the Digital Twin we are interested in and the UUID of the event that mentions it, thus we feed this SPARQL query in the following endpoint:

> SPARQL QUERY

```
/events/gdbsparql
```

Once we get the 2 UUIDs we were looking for, we can trigger the Data Pull. We feed the event UUID and a new SPARQL query (asking for details over the specific Digital Twin) into the following endpoint:

> SPARQL QUERY

```
/datapull/request/{eventUUID}
```

The API will interrogate the Corda vault extracting the identity information of the node that shared the event, and will then forward the query to that node.  
The node will run such query on its own private repository of GraphDB and will add the result to a new transaction shared with the interrogating node.  
This endpoint will return a UUID of the state containing the result.

Eventually, the Dutch node can inspect the result using the aforementioned UUID in the endpoint:

```
/datapull/retrieve/{uuid}
```

The previous examples are meant to showcase the main features in their basic form. The BDI can be further developed to increase the complexity of such features – for instance creating a more complex distribution algorithm or a more secure data pull where the receiving node applies some kind of access control policy.