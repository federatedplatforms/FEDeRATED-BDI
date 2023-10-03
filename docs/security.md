# Security

## API security consideration

The FEDeRATED Node API endpoints are not secured by default, however we highly recommend to properly secure the API endpoints
using for example an API gateway or Ingress. Primary reason to not integrate an authentication mechanism is because most parties already have existing authentication 
mechanisms in place (like an (external) OAUTH provider). 

## TLS

For production like environment it's highly recommended to use transport layer security (HTTPS) for the API endpoints. For example
using https://letsencrypt.org/ or any other certificate authority.

## iSHARE

For node to node authentication an iSHARE integration is provided. This is disabled by default but can be enabled through configuration, please refer to the [iSHARE documentation](ishare.md).