package de.uniba.dsg.serverless.pipeline.model.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * <p>
 * This is the central configuration class were all config values for the pipeline is stored. <br/> Currently
 * benchmarking and calibration is supported. Start the web application and configure the setup with respect to the
 * information shown there.
 * </p>
 * <p>
 * Do NOT change this class, despite you want to introduce new functionality to the pipeline. Make a pull request
 * therefore :)
 * </p>
 */
@Data
@Entity
@NoArgsConstructor
public class SetupConfig {

    @Id
    private String setupName;
    private boolean calibrationDeployed = false;
    @OneToOne(cascade = CascadeType.ALL)
    private BenchmarkConfig benchmarkConfig;
    @OneToOne(cascade = CascadeType.ALL)
    private CalibrationConfig calibrationConfig;

    public SetupConfig(String name) {
        this.setupName = name;
        this.benchmarkConfig = new BenchmarkConfig(this);
        this.calibrationConfig = new CalibrationConfig();
    }
}