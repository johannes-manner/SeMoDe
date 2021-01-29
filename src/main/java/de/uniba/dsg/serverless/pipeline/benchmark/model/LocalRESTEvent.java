package de.uniba.dsg.serverless.pipeline.benchmark.model;

import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class LocalRESTEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToOne(cascade = CascadeType.ALL)
    private ProviderEvent providerEvent;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean erroneous = false;
    @ManyToOne(cascade = {})
    private BenchmarkConfig benchmarkConfig;
}
