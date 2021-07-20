package de.uniba.dsg.serverless.pipeline.rest.controller;

import de.uniba.dsg.serverless.pipeline.rest.security.AuthenticationRequest;
import de.uniba.dsg.serverless.pipeline.rest.security.AuthenticationService;
import de.uniba.dsg.serverless.pipeline.rest.security.JWTTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

@Slf4j
@RestController
@RequestMapping("api")
@Tag(name = "Authentication Controller", description = "Endpoints for the credential management.")
public class AuthenticationController {

    private AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Request to get a Bearer Authentication Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created the token.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JWTTokenResponse.class))}),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)})
    @PostMapping("/login")
    public ResponseEntity createCustomer(@RequestBody AuthenticationRequest request) {
        return new ResponseEntity<>(authenticationService.generateJWTToken(request.getUsername(), request.getPassword()), HttpStatus.OK);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handleEntityNotFoundException(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}
