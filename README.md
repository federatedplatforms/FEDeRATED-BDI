# FEDeRATED Node prototype

This repository contains the FEDeRATED Node prototype implementation.

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

```mermaid
graph TD
    subgraph Cora Node 
        CORDA(Cora Node) -- events --> GRAPHDB(GraphDB)
    end
    subgraph Cora Node
        CORDA -- amqp --> OTHER
        OTHER(Corda Node) -- events --> OTHERGRAPHDB(GraphDB)
    end
```

## Event distribution

Events are distributed to other nodes based on the Nodes configuration. There are a few options how to configure the distribution mechanism:

* static: messages are routed to preconfigured destinations
* broadcast: messages are broadcast to all nodes in the network (based on the nodes in the Network Map Service).
* sparql: based on the outcome of a SPARQL ASK messages are routed to the provided destination(s).

### Static distribution

An example configuration of static distribution:

```properties
# When there are no rules explicit rules enabled, the broadcast rule will be enabled by default
# Comma separated list of rules, rules defined here are executed in the order specified
bdi.event.distribution.rules.list=static

# Comma separated list of static destinations, all events will be sent to the locations specified here.
bdi.event.distribution.rules.static.destinations=DCA/Schiphol/NL
```

### Broadcast

In broadcast mode the node uses the Network Map Service to discover that nodes and these are used to broadcast an event to. Each node maintains
a cache of the Network Map that automatically refreshes. Important note that the nodes in the Network Map should be reachable by the node distributing the events. 
If not, the events will be distributed to the nodes that are available, but the Corda flow will never complete.

```properties
# When there are no rules explicit rules enabled, the broadcast rule will be enabled by default
# Comma separated list of rules, rules defined here are executed in the order specified
bdi.event.distribution.rules.list=broadcast
```

## API usage

A FEDeRATED Node can support any number of events. The API was designed in such a way that it can be configured what events
are supported and how incoming events should be converted the expected internal format of the FEDeRATED Node: RDF

There is an introspection API for the event configuration. The `/event-types` endpoint can be called to retrieve all the supported/configured
events of a node. The configuration includes:

* eventType: unique key for an event
* name: the name of the event in the FEDeRATED ontology (part after the # https://ontology.tno.nl/logistics/federated/Event#)
* rml: RML mapping file for the conversion of an incoming JSON event to RDF
* shacl: SHACL shape for the validation of an incoming event

For each eventType a call to the `/events` endpoint can be made to submit new events, see the section: Creating an event.

Example configuration:

```properties
bdi.federated.event.types[0].eventType=federated.events.load-event.v1
bdi.federated.event.types[0].name=LoadEvent
bdi.federated.event.types[0].rml=classpath:rml/EventMapping.ttl
bdi.federated.event.types[0].shacl=classpath:shacl/LoadEvent.ttl
```

If no SHACL validation is required, one could omit this property.

```properties
bdi.federated.event.types[1].eventType=federated.events.arrival-event.v1
bdi.federated.event.types[1].name=ArrivalEvent
bdi.federated.event.types[1].rml=classpath:rml/EventMapping.ttl
```

Both the rml and shacl follow the Spring resource syntax, please refer to the Spring documentation on how to specify resources: https://docs.spring.io/spring-framework/reference/core/resources.html  

### Creating an event

When creating new events a JSON payload has to be POST-ed to the `/events` endpoint. The JSON payload will be converted to RDF by reading the `Event-Type` header and looking up the RML file matching the provided `Event-Type` value. After converting the JSON payload to RDF an optional SHACL validation is triggered.
In case of any validation errors a HTTP BAD_REQUEST (400) response will be generated. If the validation is successful then the event will be distributed to the configured destinations and a HTTP CREATED (201) will be returned. The
`Location` header in the response will contain a reference to the URI where the event can be accessed that was just created.

```bash
curl -X 'POST' \
  'http://localhost:10050/events' \
  -H 'accept: */*' \
  -H 'Event-Type: federated.events.load-event.v1' \
  -H 'Content-Type: application/json' \
  -d '{ "event" : "data" }'
```

For more info also refer to the OpenAPI specification which is available via the SwaggerUI page: http://node:port/swagger-ui/index.html

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
| [graphdb](graphdb/)                   | GraphDB repository configuration for bdi and private repositories               |
| [http](http/)                         | Example HTTP requests demonstrating how to invoke the API application           |
| [ishare](ishare/)                     | iSHARE specific integration code                                                | 


## Changelog

Please refer to the [changelog](CHANGELOG.md).

## License

Project [license](LICENSE.md).   
