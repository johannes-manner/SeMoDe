package de.uniba.dsg.serverless.pipeline.calibration.model;

import de.uniba.dsg.serverless.pipeline.model.CalibrationPlatform;
import de.uniba.dsg.serverless.pipeline.model.config.CalibrationConfig;
import de.uniba.dsg.serverless.pipeline.model.config.MachineConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
public class CalibrationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int runNumber;
    private double cpuOrMemoryQuota;
    private double gflops;
    private Double executionTimeInS;

    private CalibrationPlatform platform;

    @Embedded
    private MachineConfig machineConfig;

    @ManyToOne(cascade = {})
    private CalibrationConfig config;

    public CalibrationEvent(int i, double quota, LinpackResult linpackResult, CalibrationPlatform platform) {
        this.runNumber = i;
        this.cpuOrMemoryQuota = quota;
        this.gflops = linpackResult.getGflops();
        this.executionTimeInS = linpackResult.getExecutionTimeInS();
        this.platform = platform;
        this.machineConfig = linpackResult.getMachineConfig();
    }
}
