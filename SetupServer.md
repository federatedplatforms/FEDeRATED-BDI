The following instructions are written for Ubuntu 20.04 LTS, but are easily adaptable to other linux servers.

# Setup
Build the nodes locally. This command uses the config in `build.gradle` so make sure your node configuration is correct.

`./gradlew deployNodes`

# Native
## Prerequisites
### JRE 8
From https://docs.azul.com/core/zulu-openjdk/install/debian
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
```
Get JRE 8 (or JDK8 if building on the server)

`sudo apt-get install zulu8-jre`


## Setup
Add `database.properties` to the node folder.

# Non-docker
Copy the `build/nodes/<node-name>` folder to the server, for example using scp.

# Docker
```
docker run -ti \
 --memory=2048m \
 --cpus=2 \
 -v /home/graafewd/platform-corda/build/nodes/Netherlands_DCA:/etc/corda \
 -v /home/graafewd/platform-corda/build/nodes/Netherlands_DCA/certificates:/opt/corda/certificates \
 -v /home/graafewd/platform-corda/build/nodes/Netherlands_DCA:/opt/corda/persistence \
 -v /home/graafewd/platform-corda/build/nodes/Netherlands_DCA/logs:/opt/corda/logs \
 -v /home/graafewd/platform-corda/build/nodes/Netherlands_DCA/cordapps:/opt/corda/cordapps \
 -v /home/graafewd/platform-corda/build/nodes/Netherlands_DCA/additional-node-infos:/opt/corda/additional-node-infos \
 -v /home/graafewd/platform-corda/build/nodes/Netherlands_DCA/network-parameters:/opt/corda/network-parameters \
 -p 10012:10200 \
 -p 10201:10201 \
 corda/corda-zulu-java1.8-4.5.8:latest
```
Make sure the local paths and ports are correct.

# Client
`java -Dserver.port=10050 -Dconfig.rpc.host=localhost -Dconfig.rpc.port=10012 -Dconfig.rpc.username=user1 -Dconfig.rpc.password=test -jar clients-0.1.jar`
https://stackoverflow.com/questions/58779247/how-to-deploy-corda-nodes-and-corresponding-spring-boot-apis

# Graphdb
`docker run -p 7200:7200 -v /opt/graphdb-data:/opt/graphdb/data --name graphdb-node-1 -t khaller/graphdb-free:9.8.0`

Import shacl file: target graph named graph `http://rdf4j.org/schema/rdf4j#SHACLShapeGraph`
import ttl zip as default
// TODO which ttls? 


{
"digitalTwins": [
{
"content": "0291381e-4866-45c6-8265-a60c4aee2332",
"type": "CARGO"},{
"content": "0291381f-4866-45c6-8265-a60c4aee2332",
"type": "GOOD"
}
],
"ecmruri": "string",
"milestone": "START",
"time": "2021-11-17T09:25:22.281Z",
"id": "string",
"uniqueId": true
}

@base <http://example.com/base/> . @prefix pi: <https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#> . @prefix classifications: <https://ontology.tno.nl/logistics/federated/Classifications#> . @prefix dcterms: <http://purl.org/dc/terms/> . @prefix LogisticsRoles: <https://ontology.tno.nl/logistics/federated/LogisticsRoles#> . @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . @prefix owl: <http://www.w3.org/2002/07/owl#> . @prefix Event: <https://ontology.tno.nl/logistics/federated/Event#> . @prefix ReusableTags: <https://ontology.tno.nl/logistics/federated/ReusableTags#> . @prefix businessService: <https://ontology.tno.nl/logistics/federated/BusinessService#> . @prefix DigitalTwin: <https://ontology.tno.nl/logistics/federated/DigitalTwin#> . @prefix skos: <http://www.w3.org/2004/02/skos/core#> . @prefix xsd: <http://www.w3.org/2001/XMLSchema#> . @prefix ex: <http://example.com/base#> . @prefix time: <http://www.w3.org/2006/time#> . @prefix dc: <http://purl.org/dc/elements/1.1/> . @prefix era: <http://era.europa.eu/ns#> .  ex:Event-test12345 a Event:Event, owl:NamedIndividual;   rdfs:label "GateOut test", "Planned gate out";   Event:hasTimestamp "2019-09-22T06:00:00"^^xsd:dateTime;   Event:hasDateTimeType Event:Planned;   Event:involvesDigitalTwin ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def, ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:involvesBusinessTransaction ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:hasMilestone Event:End;   Event:hasSubmissionTimestamp "2019-09-17T23:32:07"^^xsd:dateTime .  ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def a DigitalTwin:TransportMeans,     owl:NamedIndividual .  ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a businessService:Consignment,     owl:NamedIndividual;   businessService:consignmentCreationTime "2021-05-13T21:23:04"^^xsd:dateTime;   businessService:involvedActor ex:LegalPerson-Maersk .  ex:LegalPerson-Maersk a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;   businessService:actorName "Maersk" .  ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a DigitalTwin:Equipment, owl:NamedIndividual;   rdfs:label "MNBU0494490" .



















{
"digitalTwins": [
{
"content": "0291381e-4866-45c6-8265-a60c4aee2332",
"type": "CARGO"},{
"content": "0291381f-4866-45c6-8265-a60c4aee2332",
"type": "GOOD"
}
],
"ecmruri": "string",
"milestone": "START",
"time": "2021-11-18T17:50:35.330Z",
"id": "string",
"uniqueId": true,
"fullEvent": "@base <http:\/\/example.com\/base\/> . @prefix pi: <https:\/\/ontology.tno.nl\/logistics\/federated\/PhysicalInfrastructure#> . @prefix classifications: <https:\/\/ontology.tno.nl\/logistics\/federated\/Classifications#> . @prefix dcterms: <http:\/\/purl.org\/dc\/terms\/> . @prefix LogisticsRoles: <https:\/\/ontology.tno.nl\/logistics\/federated\/LogisticsRoles#> . @prefix rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#> . @prefix owl: <http:\/\/www.w3.org\/2002\/07\/owl#> . @prefix Event: <https:\/\/ontology.tno.nl\/logistics\/federated\/Event#> . @prefix ReusableTags: <https:\/\/ontology.tno.nl\/logistics\/federated\/ReusableTags#> . @prefix businessService: <https:\/\/ontology.tno.nl\/logistics\/federated\/BusinessService#> . @prefix DigitalTwin: <https:\/\/ontology.tno.nl\/logistics\/federated\/DigitalTwin#> . @prefix skos: <http:\/\/www.w3.org\/2004\/02\/skos\/core#> . @prefix xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#> . @prefix ex: <http:\/\/example.com\/base#> . @prefix time: <http:\/\/www.w3.org\/2006\/time#> . @prefix dc: <http:\/\/purl.org\/dc\/elements\/1.1\/> . @prefix era: <http:\/\/era.europa.eu\/ns#> .  ex:Event-test12345 a Event:Event, owl:NamedIndividual;   rdfs:label \"GateOut test\", \"Planned gate out\";   Event:hasTimestamp \"2019-09-22T06:00:00Z\"^^xsd:dateTime;   Event:hasDateTimeType Event:Planned;   Event:involvesDigitalTwin ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def, ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:involvesBusinessTransaction ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:hasMilestone Event:End;   Event:hasSubmissionTimestamp \"2019-09-17T23:32:07\"^^xsd:dateTime .  ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def a DigitalTwin:TransportMeans,     owl:NamedIndividual .  ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a businessService:Consignment,     owl:NamedIndividual;   businessService:consignmentCreationTime \"2021-05-13T21:23:04\"^^xsd:dateTime;   businessService:involvedActor ex:LegalPerson-Maersk .  ex:LegalPerson-Maersk a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;   businessService:actorName \"Maersk\" .  ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a DigitalTwin:Equipment, owl:NamedIndividual;   rdfs:label \"MNBU0494490\" .",
"countriesInvolved": [
"NL"
]
}