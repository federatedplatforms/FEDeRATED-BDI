# Notifications / Webhook

## How it works

FEDeRATED nodes can send and receive events from other nodes in the network. A FEDeRATED Node provides a mechanism to notify (external) systems
whenever new events are received by a node. These notifications are sent in the form of a request to an HTTP endpoint. 
It is mandatory that the configured endpoints are accessible by the node that tries to invoke them.

A node runs a scheduled job (every 15 seconds) that polls for changes in the nodes event store. It uses the Corda Vault to query for new events.
It will send notifications for new events that arrive since the node started or since the last successful poll.

Notification are configurable through a node's Webhook API. THe following section describes how to use the Webhooks API.

## Adding a Notification / Webhook

```http request
POST http://localhost:10050/api/webhooks
Content-Type: application/json

{
  "clientId": "client-id-federated.events.minimal.v1",
  "eventType": "federated.events.minimal.v1",
  "callbackURL": "http://my-client.link"
}
```

optionally a tokenURL and extra variables for the acquirement of the token can be added 
The extra variables are used to crete the JWT and can contain 
For this feature a keystore is used containing the private key that is used to sign the jwt. 
The information of the keystore is enabled and configured in the federated.node.keystore a

```configuration
federated.node.keystore.enabled=<true/false>
federated.node.keystore.password=<bcrypt encrypted password> 
federated.node.keystore.location=<name of the keystore>
federated.node.keystore.type=<type of keystore>
federated.node.keystore.alias=<certificat alias> 
```

```http request
POST http://localhost:10050/api/webhooks
Content-Type: application/json

{
  "clientId": "client-id-federated.events.minimal.v1",
  "eventType": "federated.events.minimal.v1",
  "callbackURL": "http://my-client.link"
  "tokenURL": "https://my-token.link"
  "extraVariables": "{'var1':'val1','val2':'val2'}"
}
```

## Update a Webhook

Updating a Webhook can be done using the POST http method. The clientId needs to be provided as a path parameter.

```http request
POST http://localhost:10050/api/webhooks
Content-Type: application/json

{
  "clientId": "client-id-federated.events.minimal.v1",
  "eventType": "federated.events.minimal.v1",
  "callbackURL": "http://my-client.link"
  "tokenURL": "https://my-token.link"
  "extraVariables": "{'var1':'val1','val2':'val2'}"
}
```

## Delete a Webhook

Removing a Webhook can be done using the DELETE http method. The clientId needs to be provided as a path parameter.

```http request
DELETE http://localhost:10050/api/webhooks/client-id-federated.events.minimal.v1
```

## Callback payload

Whenever a new event comes in of type `federated.events.minimal.v1` then all the Webhooks that are registered for that event type
will get a notification. The structure of the notification is shown below. Inside the payload the eventType is specified, but also the
eventUUID. With this information the receiver of the notification could fetch the event using the Events API.

```json
{
    "eventType" : "federated.events.minimal.v1",
    "eventUUID" : "some-uuid"
}
```
