package com.soda.risk.engine.config.disposer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DisposerInfoService extends ServiceImpl<DisposerInfoMapper, DisposerInfo> {

    public List<DisposerInfo> getActiveByUserId(String userId) {
        return list(new LambdaQueryWrapper<DisposerInfo>()
                .eq(DisposerInfo::getUserId, userId)
                .eq(DisposerInfo::getState, 1));
    }

    public boolean releasePunish(String userId) {
        List<DisposerInfo> list = getActiveByUserId(userId);
        for (DisposerInfo info : list) {
            info.setState(0);
            info.setUpdateTime(LocalDateTime.now());
            updateById(info);
        }
        log.info("Released punish for userId={}", userId);
        return true;
    }

    public boolean releasePunishBatch(List<String> userIds) {
        for (String userId : userIds) {
            releasePunish(userId);
        }
        return true;
    }
}
