package de.uniba.dsg.serverless.pipeline.rest.controller;

import de.uniba.dsg.serverless.pipeline.rest.service.RestBenchmarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.ForbiddenException;

@Slf4j
@RestController
@RequestMapping("api/benchmark")
public class RestBenchmarkController {

    private RestBenchmarkService restBenchmarkService;

    @Autowired
    public RestBenchmarkController(RestBenchmarkService restBenchmarkService) {
        this.restBenchmarkService = restBenchmarkService;
    }

    @GetMapping("{id}")
    public ResponseEntity getBenchmarkDto(@PathVariable("id") Long id) {
        return ResponseEntity.ok(restBenchmarkService.getBenchmarkConfig(id));
    }

    @GetMapping("{id}/data")
    public ResponseEntity getBenchmarkData(@PathVariable("id") Long id) {
        return ResponseEntity.ok(restBenchmarkService.getBenchmarkData(id));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handleEntityNotFoundException(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity handleForbiddenException(ForbiddenException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity handleInternalServerException(RuntimeException ex) {
        return new ResponseEntity<>("Open an issue at GitHub!", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
