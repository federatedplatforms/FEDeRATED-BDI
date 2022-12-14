# iSHARE implementation notes

In order to use iSHARE in Flows in a Federated Corda implementation, a set of tools (in the form of Kotlin classes and data classes) has been created to make integration into Corda Workflows easier.
The library handles all communication with external services (in this case the iSHARE scheme/satellite). 

## Prerequisites

 - Membership of the iSHARE scheme
 - iSHARE certificate (eIDAS)
 - HTTPS access to the configured iSHARE scheme

## Usage

The main entry point of the library is the ISHAREClient. From this component all functionality needed to request , use and check an access token is provided.
The HTTPClient can be instantiated with a property filename or without (this will use the default "ishare.properties" filename)

### config 
The property file should contain the following values : 

| property         | type     | description                                        |
|------------------|----------|----------------------------------------------------|
| ishare.EORI      | String   | The iSHARE EORI formatted as EU.EORI.\<iSHARE ID\> |
| ishare.key       | String   | The iSHARE private key in BASE64 encoded PEM format|
| ishare.cert      | String   | The iSHARE public key in BASE64 encoded PEM format |
| ishare.pass      | String   | The password for the iSHARE private key            |
| ishare.schemeURL | String   | The URL for the iSHARE scheme/Satelite             |
| ishare.schemeID  | String   | The iSHARE id for the scheme/satellite participant | 
| ishare.enabled   | Boolean  | Enable iSHARE (default = false)                     | 



## Structure

The library contains both service consumer and service provider tools that help you create the JWTs needed to produce all required information needed in the IA process.
IT also contains management of access tokens.

### Model package
The model package holds object and message definitions used during the iSHARE IA process.
The token, request and response structures are designed according to the iSHARE specifications.
The ISHAREAccessToken class is used by the ISHAREClient to store and maintain the access tokens


#### party package
The PartiesToken (wrapped in a PartiesResponse) defines the format the iSHARE scheme sends in response to a party information request (/parties?eori=<'eori'>)
and is used to determine if a participant is active in the scheme and signed all agreements to that scheme.

#### token package
The token package holds all classes needed to request an access token.
ClientAssertion and ConsumerAccessToken define the format of the JWT used to in acquiring an access token from a service provider.
ISHAREAccessToken is used in the management of acquired access tokens.
ISHARETokenRequest and ISHARETokenResponse define the content of the request and reply of the /connect/ API endpoint as defined the iSHARE specification.

### Utils package
The Utils package contains useful classes that help during the access token communication
The ISHAREHTTPClient removes the certificate check for https requests. This is done because the iSHARE test scheme uses a selfsigned certificate.
The selfsigned certificate can be added to the trusted list but to make things easier for demo purposes the disabling option has been chosen.
When using this library in production this class should be altered to remove it.

The ISHAREPemReaderUtil helps reading public and private key from the configuration where the certificate are stored in BASE64 encoded PEM formats

### ISHAREClient
This class holds everything needed by a service provider or service consumer to request , create and verify client assertions and access tokens.


## Usage

### Service Consumer
After creating the configuration for iSHARE the ISHAREClient is used to request an access token from a  service provider.
If a token has been previously requested for this service provider and the token is still valid, this stored token will be used. If the token has expired a new token will be requested by the ISHAREClient.
With this token an API request can be send to the service provider using the token as a 'Bearer Token' .
Using the ISHAREHTTPClient this is handles for you and accessing a iSHARE enabled API end point is handled in one step.

```kotlin
private val iSHAREHTTPClient: ISHAREHTTPClient,
private val accessTokenUtil: AccessTokenUtil

val stringEntity: HttpEntity = StringEntity(mapper.writeValueAsString(delegationMask), ContentType.APPLICATION_JSON)
val httpPost = HttpPost(capabilityURLs["delegation"])
httpPost.entity = stringEntity
try {
    iSHAREHTTPClient.sendRequest(httpPost, accessTokenUtil.getToken("http://exaplme.com/dosomething","EORI.EU.NL0000001")).use { response ->
        if (response.statusLine.statusCode == 200) {
            // your code here
        }
    }
} catch (e: IOException) {
    // your code here
}
```


### Service Provider
As an iSHARE enabled Service provider several API end point must be implemented to adhere to the iSHARE specifications
This library gives the tools to handle the /connect API End Point.

```kotlin
    private val iSHAREClient = ISHARECient()

    @PostMapping(value = ["/connect"], produces = ["application/json"])
    private fun connect(@RequestBody tokenRequest: ISHARETokenRequest): ResponseEntity<String> {
        if (iSHARECLient.checkTokenRequest(tokenRequest) &&
            iSHARECLient.checkPartyWithScheme(tokenRequest.client_id)
          
            return ResponseEntity.status(200)
                .header("Content-Type", "application/text")
                .body("ishareClient.createTokenResponse(tokenRequest.client_id)")
    }
```

## Event Sharing process using iSHARE 

``` mermaid
sequenceDiagram
title Federated iSHARE process
participant cna as Corda Node A
participant cnb as Corda Node B 
participant is as iSHARE Scheme

activate cna
cna->>cna: process event, determine receipients
cna->>cna: create iSHARE client assertion
cna->>cnb: request access token using client assertion
activate cnb
cnb->>cnb: verify client assertion
opt failed verification
cnb->>cna: verification failed
end
cnb->>cnb: create client assertion
cnb->>is: request access token
activate is
is->>is: verify client assertion
opt failed verification
is->>cnb: verification failed
cnb->>cna: failed to verify iSHARE membership
end
is->>is: verify scheme membership
opt failed verification
is->>cnb: failed to verify iSHARE membership
cnb->>cna: failed to verify iSHARE membership
end
is->>is: create access token
is->>cnb: access token
cnb->>is: request membership information(Corda Node A) with access token
is->>is: verify access token
opt failed verification
is->>cnb: failed to verify access token
cnb->>cna: failed to verify iSHARE membership
end
is->>is: retrieve membership information(cna) access token
is->>cnb:membership information(cna)
deactivate is
opt not a member
cnb->>cna: failed, CNA is not a members
end
cnb->>cnb: create access token
cnb->>cna: access token
cna->>cnb: eventdata (includes access token)
cnb->>cnb: verify access token
opt failed verification
cnb->>cna: failed to verify access token
end
cnb->>cnb: process event
cnb->>cna: process result
deactivate cnb
deactivate cna

```


# Sources

https://dev.ishare.eu/index.html