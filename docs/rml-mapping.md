# RMLMapper

The API project uses the RMLMapper library from RML.io: https://github.com/RMLio/rmlmapper-java which is a Java library that can convert JSON to RDF format.

## YARRRML

The resources folder contains both ttl and yml files. The yml files or a more concise way of defining the mapping files and can be used to generate the ttl files with: https://rml.io/yarrrml/matey/

# RML Mapping and validation process

Incoming events are converted to RDF using RML mapping. For each event type a mapping file needs to be specified. 
Optionally a SHACL can be specified to verify incoming events. 

The configuration of the events, their RMl and optional SHACL can be found in the application.properties of the API project. An example configuration below:

```properties
federated.node.event.types[0].eventType=federated.events.minimal.v1
federated.node.event.types[0].rml=classpath:rml/MinimalEvent.ttl
federated.node.event.types[0].schemaDefinition=http://link.to/schema/definition
# SHACL is optional and not specified for this event
# federated.node.event.types[0].shacl=classpath:shacl/MinimalEvent.ttl
```

The rml and shacl properties follow the Spring Resource definition syntax (see: https://docs.spring.io/spring-framework/reference/core/resources.html).