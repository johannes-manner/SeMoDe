package de.uniba.dsg.serverless.pipeline.calibration.model;

import de.uniba.dsg.serverless.pipeline.model.config.CalibrationConfig;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class CalibrationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int runNumber;
    private double cpuOrMemoryQuota;
    private double gflops;

    @ManyToOne(cascade = {})
    private CalibrationConfig config;

    public CalibrationEvent(int i, double quota, double executeBenchmark) {
        this.runNumber = i;
        this.cpuOrMemoryQuota = quota;
        this.gflops = executeBenchmark;
    }
}
