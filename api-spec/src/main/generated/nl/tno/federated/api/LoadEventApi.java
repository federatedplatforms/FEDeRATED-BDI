/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (6.6.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package nl.tno.federated.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import nl.tno.federated.api.model.LoadEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Generated;
import javax.validation.Valid;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-08-22T10:58:21.998+02:00[Europe/Amsterdam]")
@Validated
@Tag(name = "LoadEvent", description = "the LoadEvent API")
public interface LoadEventApi {

    /**
     * POST /LoadEvent : Create a resource of type LoadEvent.
     * Create a resource of type LoadEvent.
     *
     * @param loadEvent  (required)
     * @return Successfully created the resource. (status code 200)
     *         or Bad Request (status code 400)
     *         or Unauthorised (status code 401)
     *         or Unsupported Media Type (status code 415)
     */
    @Operation(
        operationId = "loadEventPost",
        summary = "Create a resource of type LoadEvent.",
        description = "Create a resource of type LoadEvent.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successfully created the resource.", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = LoadEvent.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorised"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type")
        }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/LoadEvent",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    ResponseEntity<LoadEvent> loadEventPost(
        @Parameter(name = "LoadEvent", description = "", required = true) @Valid @RequestBody LoadEvent loadEvent
    );


    /**
     * GET /LoadEvent/{resourceId} : Retrieve a resource of type LoadEvent by id.
     * Retrieve a resource of type LoadEvent by id.
     *
     * @param resourceId  (required)
     * @return Successful operation. (status code 200)
     *         or Bad Request (status code 400)
     *         or Unauthorised (status code 401)
     *         or Resource not found (status code 404)
     *         or Unsupported Media Type (status code 415)
     */
    @Operation(
        operationId = "loadEventResourceIdGet",
        summary = "Retrieve a resource of type LoadEvent by id.",
        description = "Retrieve a resource of type LoadEvent by id.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation.", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorised"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type")
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/LoadEvent/{resourceId}",
        produces = { "application/json" }
    )
    ResponseEntity<String> loadEventResourceIdGet(
        @Parameter(name = "resourceId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("resourceId") String resourceId
    );

}
