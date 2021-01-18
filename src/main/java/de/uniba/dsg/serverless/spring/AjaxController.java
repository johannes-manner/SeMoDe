package de.uniba.dsg.serverless.spring;

import de.uniba.dsg.serverless.pipeline.benchmark.model.BenchmarkMode;
import de.uniba.dsg.serverless.util.SeMoDeException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AjaxController {

    @GetMapping("benchmark/mode/{tag}")
    public BenchmarkMode getMode(@PathVariable(value = "tag") String tag) throws SeMoDeException {
        return BenchmarkMode.fromString(tag);
    }
}
