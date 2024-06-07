# FEDeRATED Node prototype

This repository contains the FEDeRATED Node prototype implementation.

## Logical flow

In the following diagram the logical steps are described how the FEDeRATED Node processes incoming events. 
An ontology for the FEDeRATED node is maintained using Semantic Treehouse (https://www.semantic-treehouse.nl/).
The RML and SHACL are based on the ontology as defined in Semantic Threehouse.

```mermaid
flowchart TD 
 
     Client(Client) -- "new event json" --> API[API receives new event]
     
     subgraph node[Create new event flow]
        API --> Enrich[Add an UUID to the new event]
        Enrich --> Convert[Convert event from JSON to RDF]
        Convert --> Validate[Perform SHACL validation on RDF]
        Validate --> Distribute[Distribution event RDF to other nodes]
        Distribute --> Storage[Store triples into triple store]
    end
    
    Distribute --> Other(Other Nodes)
    STH(Semantic Treehouse) -- RML --> Convert
    STH -- SHACL --> Validate
    Storage --> Triple(Triple Store)
```

The following diagram describes how each node's local triple store can be SPARQL-ed:

```mermaid
flowchart TD 
 
     Client -- "SPARQL" --> API
     
     subgraph node[SPARQL flow]
        API -- execute --> Triple(Triple Store)
    end
```

## Components

A FEDeRATED Node is composed by the following components:

- API
- Corda node
- GraphDB

It uses the following components from the Corda Network:

- Notary
- Network Map Service

As illustrated in the diagram below:

```mermaid
graph TD
    subgraph FEDeRATED Node 
        API -- events --> CORDA(Corda Node)
        CORDA -- events --> GRAPHDB(GraphDB)
    end
    subgraph Corda Network
        API --> NMS
        API -- sparql --> GRAPHDB
        CORDA --> NOTARY
        CORDA --> NMS
        NOTARY(Corda Notary) --> NMS(Network Map Service)   
    end
```

A FEDeRATED Node can distribute events to other nodes in the network, the Corda Node takes care of the distribution. Corda Nodes communicate peer-to-peer using AMQP. 
Important to note that when deploying nodes that they should be able to access each other on their p2p address. 

```mermaid
graph TD
    subgraph Corda Node 
        CORDA(Corda Node) -- events --> GRAPHDB(GraphDB)
    end
    subgraph Corda Node
        CORDA -- amqp --> OTHER
        OTHER(Corda Node) -- events --> OTHERGRAPHDB(GraphDB)
    end
```

## Event distribution

Events are distributed to other nodes based on the Nodes configuration. There are a few options how to configure the distribution mechanism:

* static: messages are routed to pre-defined destinations
* broadcast: messages are broadcast to all nodes in the network (based on the nodes in the Network Map Service)
* sparql: based on the outcome of a SPARQL ASK messages are routed to the provided destination(s)

### Static distribution

An example configuration of static distribution:

```properties
# When there are no rules explicit rules enabled, the broadcast rule will be enabled by default
# Comma separated list of rules, rules defined here are executed in the order specified
federated.node.event.distribution.rules.list=static

# Comma separated list of static destinations, all events will be sent to the locations specified here (format: O=ORGANISATION,L=LOCALITY,C=COUNTRY).
federated.node.event.distribution.rules.static.destinations=O=DCA,L=Schiphol,C=NL
```

### Broadcast

In broadcast mode the node uses the Network Map Service to discover that nodes and these are used to broadcast an event to. Each node maintains
a cache of the Network Map that automatically refreshes. Important note that the nodes in the Network Map should be reachable by the node distributing the events. 
If not, the events will be distributed to the nodes that are available, but the Corda flow will never complete.

```properties
# When there are no rules explicit rules enabled, the broadcast rule will be enabled by default
# Comma separated list of rules, rules defined here are executed in the order specified
federated.node.event.distribution.rules.list=broadcast
```

## API usage

A FEDeRATED Node can support any number of events. The API was designed in such a way that it can be configured what events
are supported and how incoming events should be converted the expected internal format of the FEDeRATED Node: RDF

There is an introspection API for the event configuration. The `/api/event-types` endpoint can be called to retrieve all the supported/configured
events of a node. The configuration includes:

* eventType: unique key for an event
* name: the name of the event in the FEDeRATED ontology (part after the # https://ontology.tno.nl/logistics/federated/Event#)
* rml: RML mapping file for the conversion of an incoming JSON event to RDF
* shacl: SHACL shape for the validation of an incoming event

For each eventType a call to the `/events` endpoint can be made to submit new events, see the section: Creating an event.

Pre-configured event:

```properties
federated.node.event.types[0].eventType=federated.events.minimal-event.v1
federated.node.event.types[0].rml=classpath:rml/MinimalEvent.ttl
federated.node.event.types[0].shacl=classpath:shacl/MinimalEvent.ttl
```

If no SHACL validation is required, one could omit this property.

```properties
federated.node.event.types[0].eventType=federated.events.other-event.v1
federated.node.event.types[0].rml=classpath:rml/OtherEvent.ttl
```

Both the rml and shacl follow the Spring resource syntax, please refer to the Spring documentation on how to specify resources: https://docs.spring.io/spring-framework/reference/core/resources.html  

### Creating an event

When creating new events a JSON payload has to be POST-ed to the `/events` endpoint. The JSON payload will be converted to RDF by reading the `Event-Type` header and looking up the RML file matching the provided `Event-Type` value. After converting the JSON payload to RDF an optional SHACL validation is triggered.
In case of any validation errors a HTTP BAD_REQUEST (400) response will be generated. If the validation is successful then the event will be distributed to the configured destinations and HTTP CREATED (201) response will be returned. The
`Location` header in the response will contain a reference to the URI where the event can be accessed that was just created. An example:

```bash
curl -X 'POST' \
  'http://localhost:10050/api/events' \
  -H "Authorization: Basic YXBpdXNlcjphcGlwd2Q=" \
  -H 'Accept: */*' \
  -H 'Event-Type: federated.events.minimal-event.v1' \
  -H 'Content-Type: application/json' \
  -d '{ "event" : "data" }'
```

### Creating an event and distribute to specific destination(s)

When creating new events the event will be distributed according to the distribution rules and configured destinations. There is a way to override the destinations to which an event is sent by setting the 
`Event-Destinations` header. One could specify a comma separated list of destinations to which the event has to be sent (in Corda X500 node identification format: O=ORGANISATION,L=LOCALITY,C=COUNTRY (etc)). And example:

```bash
curl -X 'POST' \
  'http://localhost:10050/api/events' \
  -H 'Accept: */*' \
  -H "Authorization: Basic YXBpdXNlcjphcGlwd2Q=" \
  -H 'Event-Type: federated.events.minimal-event.v1' \
  -H 'Event-Destinations: O=ORGANISATION,L=LOCALITY,C=COUNTRY' \
  -H 'Content-Type: application/json' \
  -d '{ "event" : "data" }'
```

For more info also refer to the OpenAPI specification which is available via the SwaggerUI page: http://node:port/swagger-ui/index.html

### Executing a SPARQL query

Clients can perform SPARQL queries on the nodes local triple store using the `/sparql` endpoint. An example:

```bash
curl -X 'POST' \
  'http://localhost:10050/api/sparql' \
  -H "Authorization: Basic YXBpdXNlcjphcGlwd2Q=" \
  -H 'Accept: application/sparql-results+json' \
  -H 'Content-Type: text/plain' \
  -d 'select * where { ?s ?p ?o . } limit 100'
```

## Overriding the default properties

All properties mentioned above for configuring the node's supported events and distribution mechanism should be specified in a node specific configuration file. It is recommended
to use an external configuration file (spring.config.additional-location) for all the node's properties, but other options are available too. Because the node was built using Spring Boot, all the Spring Boot specific
configuration options are supported. Please refer to the Spring Boot documentation for more info: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.files

## Documentation

Technical documentation [can be found here](docs/README.md).

## Modules and directories

| module                                | description                                                                     |
|---------------------------------------|---------------------------------------------------------------------------------|
| [api](api/)                           | FEDeRATED API application that exposes endpoints for interacting with the node  | 
| [corda](corda/)                       | Contains the corda specific functionality (workflows, contracts, cordformation) | 
| [docs](docs/)                         | Technical documentation                                                         |
| [graphdb](graphdb/)                   | GraphDB repository configuration for federated node and private repositories    |
| [http](http/)                         | Example HTTP requests demonstrating how to invoke the API application           |
| [ishare](ishare/)                     | iSHARE specific integration code                                                | 


## Changelog

Please refer to the [changelog](CHANGELOG.md).

## License

Project [license](LICENSE.md).