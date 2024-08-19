package com.hamsterhub.convert;

import com.hamsterhub.response.StrategyResponse;
import com.hamsterhub.database.dto.StrategyDTO;
import com.hamsterhub.vo.StrategyVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface StrategyConvert {
    StrategyConvert INSTANCE = Mappers.getMapper(StrategyConvert.class);

    StrategyDTO vo2dto(StrategyVO strategyVO);
    StrategyVO dto2vo(StrategyDTO strategyDTO);

    List<StrategyDTO> vo2dtoBatch(List<StrategyVO> strategies);

    StrategyResponse dto2res(StrategyDTO strategyDTO);

    List<StrategyResponse> dto2resBatch(List<StrategyDTO> strategyDTOs);
}
