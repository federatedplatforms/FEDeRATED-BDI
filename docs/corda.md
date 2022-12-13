# Corda

Corda is a Distributed Ledger Technology (DLT) that leverages blockchain concepts to keep a balance between non-repudiation / immutability and privacy that can be oriented towards the specific needs of one's use-case.

The general architecture is structured in three main components:
1. **The States** – where the data model is defined
2. **The Flows** – where transactions are built and the communication with other nodes is handled 
3. **The Contracts** – where the constraints for valid transactions are set

For more details over Corda functioning, check the [official documentation](https://docs.r3.com/en/platform/corda/4.8/open-source/key-concepts.html).

In our codebase you can find all Corda-related code in the `corda` folder, States and Contracts in the `contracts` sub-folder and flows in `workflows`.

Our design is currently highly focused on flexibility, hence:
- Contracts are not used because no rule for valid transactions is enforced. The rationale is that the threats that the use of contracts would prevent (attacks from within the network), as well as the consequences, are not severe, and the cost that it would entail – a stronger rigidity in the data model – doesn't outweigh the benefits.
- States do not *actually* implement the data model of the logistics use-case, they are string-based instead and the whole handling of the model is done through the ontology. This, at the cost of built-in functionalities for data management within the Corda framework, ensures high customization and later expandability of the data model.

The adopted version of Corda is 4.9, you can build and run the nodes by following the instructions in the development guide.

### Integration with GraphDB

Corda cannot handle RDF data natively, that is why its database is sided by a triple-store one (GraphDB) that acts as the main database where data is actually explorable. The Corda database ([the vault](https://docs.r3.com/en/platform/corda/4.8/open-source/key-concepts-vault.html)) only holds pieces of data for non-repudiation purpose.

The challenge of having two databases in the same node of a DLT software is to always keep them aligned – it is paramount that the Truth within the node is always unique. Securing the communication between Corda and GraphDb is the key to achieve so, that is why all calls to the database must go through the "lowest" possible level: the flows.