package com.soda.risk.engine.config.riskdecision;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlackWhiteListService extends ServiceImpl<BlackWhiteListMapper, BlackWhiteList> {

    private final RedisCacheService redisCacheService;

    public List<BlackWhiteList> getByType(String listType) {
        return list(new LambdaQueryWrapper<BlackWhiteList>()
                .eq(BlackWhiteList::getListType, listType)
                .eq(BlackWhiteList::getState, 1));
    }

    public void syncToRedis() {
        // 同步黑名单
        List<BlackWhiteList> blackList = getByType("BLACK");
        redisCacheService.replaceSet(RedisKeyConstants.BLACK_WHITE_LIST + "blacklist",
                blackList.stream().map(BlackWhiteList::getListValue).collect(Collectors.toSet()));

        // 同步白名单
        List<BlackWhiteList> whiteList = getByType("WHITE");
        redisCacheService.replaceSet(RedisKeyConstants.BLACK_WHITE_LIST + "whitelist",
                whiteList.stream().map(BlackWhiteList::getListValue).collect(Collectors.toSet()));

        log.info("Synced {} blacklist and {} whitelist items to Redis", blackList.size(), whiteList.size());
    }
}
