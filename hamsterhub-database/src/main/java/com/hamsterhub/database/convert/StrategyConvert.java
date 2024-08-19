package com.hamsterhub.database.convert;

import com.hamsterhub.database.dto.StrategyDTO;
import com.hamsterhub.database.entity.Strategy;
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
