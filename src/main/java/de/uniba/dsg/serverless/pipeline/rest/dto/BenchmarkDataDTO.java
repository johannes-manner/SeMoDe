package de.uniba.dsg.serverless.pipeline.rest.dto;

import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import lombok.Data;

import java.util.List;

@Data
public class BenchmarkDataDTO {
    private Long benchmarkId;
    private List<IPointDto> data;

    public BenchmarkDataDTO(Long benchmarkId, List<IPointDto> data) {
        this.benchmarkId = benchmarkId;
        this.data = data;
    }
}
