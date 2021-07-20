package de.uniba.dsg.serverless.pipeline.rest.controller;

import de.uniba.dsg.serverless.pipeline.rest.dto.BenchmarkConfigDTO;
import de.uniba.dsg.serverless.pipeline.rest.dto.BenchmarkDataDTO;
import de.uniba.dsg.serverless.pipeline.rest.service.RestBenchmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.ForbiddenException;

@Slf4j
@RestController
@RequestMapping("api/v1/benchmark")
@Tag(name = "Benchmark Controller", description = "Available endpoints for getting publicly available data")
public class RestBenchmarkController {

    private RestBenchmarkService restBenchmarkService;

    @Autowired
    public RestBenchmarkController(RestBenchmarkService restBenchmarkService) {
        this.restBenchmarkService = restBenchmarkService;
    }

    @Operation(summary = "Get public available fields of a benchmark configuration. Short representation of the model.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the Configuration.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = BenchmarkConfigDTO.class))}),
            @ApiResponse(responseCode = "403", description = "Not allowed to access",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Config not found",
                    content = @Content)})
    @GetMapping("{id}")
    public ResponseEntity getBenchmarkDto(@PathVariable("id") Long id) {
        return ResponseEntity.ok(restBenchmarkService.getBenchmarkConfig(id));
    }

    @Operation(summary = "Get the data of a benchmark experiment for further analysis",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the data.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = BenchmarkDataDTO.class))}),
            @ApiResponse(responseCode = "403", description = "Not allowed to access",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Config not found",
                    content = @Content)})
    @GetMapping("{id}/data")
    public ResponseEntity getBenchmarkData(@PathVariable("id") Long id) {
        return ResponseEntity.ok(restBenchmarkService.getBenchmarkData(id));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity handleEntityNotFoundException(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity handleForbiddenException(ForbiddenException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity handleInternalServerException(RuntimeException ex) {
        return new ResponseEntity<>("Open an issue at GitHub!", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
