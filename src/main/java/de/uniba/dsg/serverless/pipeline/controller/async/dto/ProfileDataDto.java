package de.uniba.dsg.serverless.pipeline.controller.async.dto;

import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import lombok.Data;

import java.util.Map;

@Data
public class ProfileDataDto {

    private IPointDto[] profileData;
    private IPointDto[] avgData;
    private double[] avgOptions;
    private double avg;
    private Map<Integer, Integer> cpuMemoryEquivalents;
    private Map<Integer, Integer> providerCpuMemoryEquivalents;
    private IPointDto[] simulatedPrice;

    public ProfileDataDto(IPointDto[] profileData, IPointDto[] avgData, double[] avgOptions, double avg,
                          Map<Integer, Integer> cpuMemoryEquivalents, Map<Integer, Integer> providerCpuMemoryEquivalents,
                          IPointDto[] simulatedPrice) {
        this.profileData = profileData;
        this.avgData = avgData;
        this.avgOptions = avgOptions;
        this.avg = avg;
        this.cpuMemoryEquivalents = cpuMemoryEquivalents;
        this.providerCpuMemoryEquivalents = providerCpuMemoryEquivalents;
        this.simulatedPrice = simulatedPrice;
    }
}
