

# Server Deployment - Instructions

The following instructions are written for Ubuntu 20.04 LTS, but are easily adaptable to other linux servers.

By default, these instructions will guide you through a _native_ setup, however there's a section at the bottom going through an _alternative using docker_. You can _run_ one or multiple modules in containers.

## Prerequisites

  - JDK 8 (or JRE if you don't need to build the application)
  - BDI node (either source code or executable)

### JDK 8

From [Zulu official documentation](https://docs.azul.com/core/zulu-openjdk/install/debian)
```
# install the necessary dependencies
sudo apt-get -q update
sudo apt-get -yq install gnupg curl 

# add Azul's public key
sudo apt-key adv \
  --keyserver hkp://keyserver.ubuntu.com:80 \
  --recv-keys 0xB1998361219BD9C9

# download and install the package that adds 
# the Azul APT repository to the list of sources 
curl -O https://cdn.azul.com/zulu/bin/zulu-repo_1.0.0-3_all.deb

# install the package
sudo apt-get install ./zulu-repo_1.0.0-3_all.deb

# update the package sources
sudo apt-get update

# Install JDK (you can change "-jdk" into "-jre" if you have
# already built the application
sudo apt-get install zulu8-jre
```

### BDI node

Clone the repository or download the jar files from a trusted source.

## Setup
### Graphdb
`docker run -p 7200:7200 -v /opt/graphdb-data:/opt/graphdb/data --name graphdb-node-1 -t khaller/graphdb-free:9.8.0`
The following is also available as the `graphdb-data` folder in the project root. If you wish to set it up yourself, or make sure that you have the latest version, you can do so by running the following commands:

1. Navigate to http://\<server>:7200
2. Setup -> Repository -> New free repository
3. Provide a name, put this name in database.properties. The default is bdi. 
4. Tick the box to "Enable SHACL validation"
5. Create
6. (Optional) click the thumb-tack icon to set the new repository as the default and run it
7. Create a zip of the ttl in [this repository](https://github.com/silenroc1/FEDeRATED-copy)
8. Import -> RDF -> Upload the ontology ttl zip 
9. Upload file userEvent.shapes.ttl. Target graph, named graph: `http://rdf4j.org/schema/rdf4j#SHACLShapeGraph`

```
node {
        name "O=HappyNode,L=Amsterdam,C=NL,OU=SmilesProvider"
        p2pAddress "localhost:10005"
        rpcSettings {
            address("localhost:10006")
            adminAddress("localhost:10046")
        }
        rpcUsers = [[ user: "user1", "password": "test", "permissions": ["ALL"]]]
    }
```

Edit the information according to your needs.

The file `workflows/database.properties` contains information over the location of the GraphDB instance.
```
triplestore.protocol=http
triplestore.host=localhost
triplestore.port=7200
triplestore.repository=federated-shacl
```

Once configuration information is set you can proceed to build the node(s).  
You can skip the following step if you have already the executable `corDapp.jar`.

From root folder:
```
./gradlew deploySingleNode
```

**Note**: you may want to build different nodes at the same time, then use other tasks like `deployNodes` and make sure to edit `build.gradle` accordingly.

The last command will create a folder under `build/nodes` containing configuration and executable files of your node. You can copy that folder to a suitable location on your server.

You also need to build the API layer based on Spring Boot:
```
./gradlew bootJar
```
This creates the file `clients-0.1.jar` under `client/build/lib`. You can copy that folder to a suitable location on your server.

## Run the modules

Modules to run:
- Corda node
- API
- GraphDB

### Corda Node

You can choose to:
- Register your node to an existing network and run it
- Run a single node
- Run all nodes on the same machine (normally for development and testing purpose)

#### Register and run your node

Adapted from [the Cordite Network Map Service FAQ](https://gitlab.com/cordite/network-map-service/blob/master/FAQ.md).

Download the network trust store:
```shell
curl https://nms.basicdatasharinginfrastructure.net/network-map/truststore -o /tmp/network-truststore.jks
```

Prepare node and run registration – make sure to run the following instructions in the node base directory (the same folder where `corda.jar` is located) 
```shell
echo 'compatibilityZoneURL="https://nms.basicdatasharinginfrastructure.net"' >> node.conf
echo 'devModeOptions.allowCompatibilityZone=true' >> node.conf
rm -rf network-parameters nodeInfo-* persistence.mv.db certificates additional-node-infos
java -jar corda.jar --initial-registration --network-root-truststore /tmp/network-truststore.jks --network-root-truststore-password trustpass
```
Corda will shut down after registration. Re-start the node normally as indicated in the following subsection, with the addition of the truststore flags.

#### Run a single node
```shell
nohup java -jar NODE_FOLDER/corda.jar --base-directory=NODE_FOLDER > log_node.txt &
```
The initial `nohup` and the final `&` are optional, they ensure the process keeps running after terminating the shell.

For a handy killing of the process at a later stage you may want to **append** to the previous command:
```
echo $! >> log_pid.txt 
```

This will log the PID of the process.

#### Run all nodes
```
build/nodes/runnodes
```
This command will run all nodes previously built on the local machine.

### API
```
java -Dserver.port=10050 -Dconfig.rpc.host=localhost -Dconfig.rpc.port=1006 -Dconfig.rpc.username=user1 -Dconfig.rpc.password=test -jar clients-0.1.jar
```
The API will be reachable at port `10050`, the rpc port should be the same selected in the initial configuration of `build.gradle`, as well as the user and password – see our earlier example:
```
node {
        name "O=HappyNode,L=Amsterdam,C=NL,OU=SmilesProvider"
        p2pAddress "localhost:10005"
        rpcSettings {
            address("localhost:10006")
            adminAddress("localhost:10046")
        }
        rpcUsers = [[ user: "user1", "password": "test", "permissions": ["ALL"]]]
    }
```

### GraphDB
[Download GraphDB](https://www.ontotext.com/products/graphdb/graphdb-free/)  distribution file and unzip it.

From [the official documentation](https://graphdb.ontotext.com/documentation/6.6/free/quick-start-guide.html):

Start the GraphDB database and Workbench interfaces in the embedded Tomcat server by executing the startup script located in the root directory:
```
./startup.sh -p 7200
```
1. Navigate to http://\<server>:7200
2. Setup -> Repository -> New free repository
3. Provide a name, it must be the same provided in `database.properties` during corda node configurtion
4. Tick the box to "Enable SHACL validation"
5. Create
6. (Optional) click the thumb-tack icon to set the new repository as the default and run it
7. Create a zip of the ttl in [this repository](https://github.com/silenroc1/FEDeRATED-copy)
8. Import RDF -> Upload RDF files -> Select the ontology ttl zip
9. Upload file event.shapes.ttl. Target graph, named graph: `http://rdf4j.org/schema/rdf4j#SHACLShapeGraph`

## Sample calls
Open http://localhost:10050/swagger-ui.html in your browser. A swagger UI should appear.
Under Corda details, one can query the Corda node what nodes it knows. It should know at least one notary (GET `/node/notaries`) and a few other nodes (GET `/node/peers`).

Play around with the `/events` calls too. In case you are prompted for an access token, you can use your iShare instance – make sure you configured it under `database.properties`, together with GraphDB, in case – or enter `Bearer doitanyway` to skip this.

See the readme file for example data.
```ttl
@base <http://example.com/base/> . @prefix pi: <https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#> . @prefix classifications: <https://ontology.tno.nl/logistics/federated/Classifications#> . @prefix dcterms: <http://purl.org/dc/terms/> . @prefix LogisticsRoles: <https://ontology.tno.nl/logistics/federated/LogisticsRoles#> . @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . @prefix owl: <http://www.w3.org/2002/07/owl#> . @prefix Event: <https://ontology.tno.nl/logistics/federated/Event#> . @prefix ReusableTags: <https://ontology.tno.nl/logistics/federated/ReusableTags#> . @prefix businessService: <https://ontology.tno.nl/logistics/federated/BusinessService#> . @prefix DigitalTwin: <https://ontology.tno.nl/logistics/federated/DigitalTwin#> . @prefix skos: <http://www.w3.org/2004/02/skos/core#> . @prefix xsd: <http://www.w3.org/2001/XMLSchema#> . @prefix ex: <http://example.com/base#> . @prefix time: <http://www.w3.org/2006/time#> . @prefix dc: <http://purl.org/dc/elements/1.1/> . @prefix era: <http://era.europa.eu/ns#> .  ex:Event-test12345 a Event:Event, owl:NamedIndividual;   rdfs:label "GateOut test", "Planned gate out";   Event:hasTimestamp "2019-09-22T06:00:00"^^xsd:dateTime;   Event:hasDateTimeType Event:Planned;   Event:involvesDigitalTwin ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def, ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:involvesBusinessTransaction ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:hasMilestone Event:End;   Event:hasSubmissionTimestamp "2019-09-17T23:32:07"^^xsd:dateTime .  ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def a DigitalTwin:TransportMeans,     owl:NamedIndividual .  ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a businessService:Consignment,     owl:NamedIndividual;   businessService:consignmentCreationTime "2021-05-13T21:23:04"^^xsd:dateTime;   businessService:involvedActor ex:LegalPerson-SomeShipper .  ex:LegalPerson-SomeShipper a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;   businessService:actorName "SomeShipper" .  ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a DigitalTwin:Equipment, owl:NamedIndividual;   rdfs:label "ABCDE" .
```

## Alternative using docker
After building the jar files the single modules can be run through docker containers.
You first need to install docker.

### Docker installation

From [docker official documentation](https://docs.docker.com/engine/install/ubuntu/):

```
# Remove older versions
sudo apt-get remove docker docker-engine docker.io containerd runc

# Set up repositories and install necessary packages
sudo apt-get update
sudo apt-get install ca-certificates curl gnupg lsb-release

# Add Docker’s official GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Use the following command to set up the repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
  
# Install docker engine
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Verify docker is installed
sudo docker run hello-world
```

### Corda node
```
docker run -ti \
 --memory=2048m \
 --cpus=2 \
 -v /path-to-node-folder:/etc/corda \
 -v /path-to-node-folder/certificates:/opt/corda/certificates \
 -v /path-to-node-folder:/opt/corda/persistence \
 -v /path-to-node-folder/logs:/opt/corda/logs \
 -v /path-to-node-folder/cordapps:/opt/corda/cordapps \
 -v /path-to-node-folder/additional-node-infos:/opt/corda/additional-node-infos \
 -v /path-to-node-folder/network-parameters:/opt/corda/network-parameters \
 -p 10012:10200 \
 -p 10201:10201 \
 corda/corda-zulu-java1.8-4.5.8:latest
```
Make sure the local paths and ports are correct.

### API
Run this command in the same folder as client-0.1.jar:
```
docker build -t corda-client
docker run -d corda-client
```
Make sure the client is on the same network as the Corda layer. If Corda is running natively, one could add `--network=host` to the second command.

### GraphDB
```
docker run -p 7200:7200 -v /opt/graphdb-data:/opt/graphdb/data --name graphdb-node-1 -t khaller/graphdb-free:9.8.0
```





