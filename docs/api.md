# FEDeRATED Node API

FEDeRATED Node API exposed all the endpoints for interaction with a node.

## Available endpoints

| Endpoint                                                                                        | Description                |
|-------------------------------------------------------------------------------------------------|----------------------------|
| [/api/corda](../api/api/src/main/kotlin/nl/tno/federated/api/controllers/CordaNodeController.kt) | Corda node related endpoints |
| [/api/events](../api/api/src/main/kotlin/nl/tno/federated/api/controllers/EventsController.kt)  | Event related endpoints    | 
| [/api/sparql](../api/api/src/main/kotlin/nl/tno/federated/api/controllers/SPARQLController.kt)  | SPARQL endpoint            |
| [/api/webhooks](../api/api/src/main/kotlin/nl/tno/federated/api/webhook/WebhookController.kt)   | Webhook endpoint           |

## API documentation

When the FEDeRATED Node API application is started, the following endpoint will be available for accessing the Swagger API documentation:

- http://localhost:10050/swagger-ui.html

In order to start the application first build and run it.


