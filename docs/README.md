# FEDeRATED Node prototype technical documentation

This document contains the technical documentation for the FEDeRATED Node prototype.

## Components

```mermaid
graph TD
    DataPull[Data Pull] -- SPARQL/HTTP --> API
    PublishEvent[New Event] -- JSON --> API
        subgraph FEDeRATED Node
            subgraph api[FEDeRATED API]
            API(Spring Boot) -- contains --> RMLMapper
            API -- contains --> EventDistribution(Event Distribution Service)
            API -- uses --> CordaRPC[CordaRPC Client]
            end
            CordaRPC -- AMQP --> CORDA[Corda]
            subgraph Corda Node [Corda Node]
                CORDA -- includes --> Contracts(Contracts)
                CORDA -- includes --> Workflows(Workflows)
                Workflows -- uses --> iSHARE(iSHARE Client)
                Workflows -- uses --> GRAPHDB(GraphDB Client)
            end
            subgraph GraphDB
                subgraph Repositories
                    GRAPHDB -- HTTP --> FEDeRATED(federated)
                    FEDeRATED -- uses --> SHACL(SHACL)
                end
            end
        end
    CORDA --> Notary[Corda Notary]
    CORDA --> NetworkMap[Corda Network Map]
    CORDA --> OtherCorda[Other Corda Nodes]    
    iSHARE -- TLS/HTTPS --> ISHARE[iSHARE]
```

## Technical documentation

The following documentation is available:

| Component                                 | Description                                               |
|-------------------------------------------|-----------------------------------------------------------|
| [API](api.md)                             | FEDeRATED API documentation                               | 
| [Corda](corda.md)                         | Corda documentation (workflows, contracts, cordformation) |
| [Development Guide](development-guide.md) | Development environment setup guide                       |
| [Deployment](deployment.md)               | Deployment guide                                          |
| [GraphDB](graphdb.md)                     | GraphDB setup guide                                       | 
| [iSHARE](ishare.md)                       | iSHARE documentation                                      | 
| [Security](security.md)                   | Security consideration                                    | 
| [RML Mapping](rml-mapping.md)             | RML mapping documentation                                 |
| [Testing](testing.md)                     | Unit and integration testing documentation                | 

### Running using Docker

The is also a Dockerized setup available on github, please refer to: https://github.com/Federated-BDI/Docker-BDI-Node


