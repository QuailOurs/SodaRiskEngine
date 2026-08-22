package com.soda.risk.engine.config.business;

import com.baomidou.mybatisplus.extension.service.IService;

public interface BusinessSideService extends IService<BusinessSide> {
    boolean existsByKey(String businessSideKey, Long excludeId);
    boolean existsByName(String name, Long excludeId);
}
