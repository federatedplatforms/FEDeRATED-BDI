# GraphDB

This page contains information about the implementation of GraphDB in the FEDeRATED BDI prototype.

## Application and motivation

Resource Description Framework (RDF) is a flexible data model with a expressive power to represent complex situations and relationships.
The directed graph made of triple statements structure enables other extensions that increase the expressiveness of the language such as RDFS, OWL, SHACL.
The RDF data model also enables people using RDF to execute computationally inexpensive queries which are easy to understand and reproduce.

GraphDB is a free triple storage database solution that runs as a web application, from a Docker image. Setting up a repository to host data in GraphDB
allows for the integration of the verification extension SHACL just from a click of a button.

The FEDeRATED BDI prototype saves event data as RDF in GraphDB, enabling the possibility of querying for events information based on the access rights given
by the uploader of the event data.

## How to run

To run the GraphDB docker open a terminal window and navigate to the root folder of the FEDeRATED BDI prototype Kotlin project and run the command:
```docker compose up```.

The [docker compose](../docker-compose.yml) file specifies the files loaded into the GraphDB server instance. The FEDeRATED ontology,
as well as the SHACL ontology are loaded in the GraphDB instance allowing users to verify if their input data is valid according to the ontology and to find
out more about the FEDeRATED ontology.

## SHACL validation

The FEDeRATED ontology describes how events as well as the resources associated to the events (locations, goods, equipments, transport means) need to be defined.
SHACL validation is used to enforce that all events input in the GraphDB instance respect the ontology structure, providing meaningful error messages if the
input triple data does not respect the ontology structure.