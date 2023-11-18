package com.hamsterhub.service.convert;

import com.hamsterhub.service.dto.StrategyDTO;
import com.hamsterhub.service.entity.Strategy;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface StrategyConvert {
    StrategyConvert INSTANCE = Mappers.getMapper(StrategyConvert.class);

    StrategyDTO entity2dto(Strategy strategy);
    Strategy dto2entity(StrategyDTO strategyDTO);

    List<StrategyDTO> entity2dtoBatch(List<Strategy> strategies);
}
