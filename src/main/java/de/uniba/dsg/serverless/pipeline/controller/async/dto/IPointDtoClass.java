package de.uniba.dsg.serverless.pipeline.controller.async.dto;

import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class IPointDtoClass implements IPointDto {
    private Double x, y;
    
}
