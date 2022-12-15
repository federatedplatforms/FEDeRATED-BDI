# Security

## API security consideration

The BDI API application endpoints are not secured and accessible by anyone. 
For production like environment it's highly recommended to use transport layer security (HTTPS) and to secure the API endpoints.
For Spring Boot applications one could integrate Spring Security for this purpose.
Alternatively use an edge service or API gateway that could provide this functionality in a more transparent way.

## iSHARE

iSHARE can be enabled for Corda nodes, please refer to the [iSHARE documentation](ishare.md).