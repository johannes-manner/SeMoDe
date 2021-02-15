package de.uniba.dsg.serverless;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// TODO extend docu here

/**
 * As in 2018, the first prototype to benchmark functions was implemented during the project in the summer term. All
 * information was created in bash files and executed from there. <br/> In 2020, the procedure changed, started with
 * AWS, that native SDKs should be used for getting the information. The benchmarking pipeline is reimplemented due to
 * this decision and other providers and open source FaaS platforms should follow.
 */
@SpringBootApplication
public class SeMoDeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeMoDeApplication.class, args);
    }
}
