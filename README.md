# FEDeRATED BDI prototype

This repository contains the FEDeRATED BDI prototype implementation. 

## How this project is organized

| module                                | description                                                                     |
|---------------------------------------|---------------------------------------------------------------------------------|
| [api](api/)                           | API application taht exposes endpoints for interacting with the BDI node        | 
| [corda](corda/)                       | Contains the corda specific functionality (workflows, contracts, cordformation) | 
| [docs](docs/)                         | Technical documentation                                                         |
| [graphdb](graphdb/)                   | GraphDB repository configuration for bdi and private repositories               |
| [http](http/)                         | Example HTTP requests demonstrating how to invoke the API application           |
| [ishare](ishare/)                     | ishare specific integration code                                                | 
| [semantic-adapter](semantic-adapter/) | Converts Tradelens (JSON) data to triples (RDF turtle) format                   | 

## Documentation

Technical documentation [can be found here](docs/README.md).

## Changelog

Please refer to the [changelog](CHANGELOG.md).

## License

Project [license](LICENSE.md).   
