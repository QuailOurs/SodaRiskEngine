package com.soda.risk.engine.config.business;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BusinessSideServiceImpl extends ServiceImpl<BusinessSideMapper, BusinessSide>
        implements BusinessSideService {

    @Override
    public boolean existsByKey(String businessSideKey, Long excludeId) {
        LambdaQueryWrapper<BusinessSide> query = new LambdaQueryWrapper<BusinessSide>()
                .eq(BusinessSide::getBusinessSideKey, businessSideKey);
        if (excludeId != null) query.ne(BusinessSide::getId, excludeId);
        return count(query) > 0;
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        LambdaQueryWrapper<BusinessSide> query = new LambdaQueryWrapper<BusinessSide>()
                .eq(BusinessSide::getName, name);
        if (excludeId != null) query.ne(BusinessSide::getId, excludeId);
        return count(query) > 0;
    }
}
