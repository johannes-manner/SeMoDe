package de.uniba.dsg.serverless;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is a short overview of the development milestones from the prototype and the history how the prototype changed
 * over time: <br/> <br/>
 * As in 2018, the first prototype to benchmark functions was implemented during the project in the summer term. All
 * information was created in bash files and executed from there. A publication for IEEE UCC Companion 2018
 * was written. <br/>
 * In 2020, the procedure changed, started with
 * AWS, that native SDKs should be used for getting the information. The benchmarking pipeline is reimplemented due to
 * this decision and other providers and open source FaaS platforms should follow. <br/>
 * In winter 2020/2021, the prototype was migrated to a full stack Spring Boot based application with a Thymeleaf
 * frontend. Publications for IEEE SOSE 2021 and IEEE CLOUD 2021 extended the functionality of the prototype.
 * Also a visualizer, where benchmarking results are now available for also non registered users was introduced.
 */
@SpringBootApplication
public class SeMoDeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeMoDeApplication.class, args);
    }
}
