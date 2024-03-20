## Deployment of a FEDeRATED Node

A FEDeRATED Node consists of the following components:

* GraphDB: triple store
* FEDeRATED Node API: standalone Java application, packaged as an executable jar
* Corda node: corda + cordapps

### Corda network

The cordapps require the presence of at least one Notary in the Corda network.

### Deployment diagram

#### Components

```mermaid
flowchart LR
    API(FEDeRATED API) -- HTTP --> GraphDB
    API(FEDeRATED API) -- RPC --> CORDA(Corda Node)
    CORDA -- HTTP --> GraphDB
    subgraph Corda Network
        direction LR
        CORDA -- AMQP --> NOTARY(Corda Notary)
    end
```

#### Containers

The docker based deployment consists of at least 3 containers per FEDeRATED Node. 

* 'khaller/graphdb-free:10.0.0' 
  * exposed port: 7200
* federated-node-api
  * exposed port: 10050
* corda
  * exposed port: 10006

```mermaid
flowchart LR
        federated-api -- HTTP --> graphdb
        federated-api -- RPC --> corda
        corda -- HTTP --> graphdb
    
```

### Onboarding

A node in the Corda network needs to be onboarded by the Corda doorman.

