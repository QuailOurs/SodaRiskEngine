package com.soda.risk.engine.config.riskdecision;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReturnCodeService extends ServiceImpl<ReturnCodeMapper, ReturnCode> {

    public List<ReturnCode> getBySceneKey(String sceneKey) {
        return list(new LambdaQueryWrapper<ReturnCode>()
                .eq(ReturnCode::getSceneKey, sceneKey)
                .eq(ReturnCode::getState, 1));
    }

    public List<String> getAllReturnCodeBySceneKey(String sceneKey) {
        return getBySceneKey(sceneKey).stream()
                .map(ReturnCode::getReturnCode)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getAllSceneKeys() {
        return list(new LambdaQueryWrapper<ReturnCode>()
                .eq(ReturnCode::getState, 1)
                .select(ReturnCode::getSceneKey))
                .stream()
                .map(ReturnCode::getSceneKey)
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean validReturnCodeExist(String returnCode) {
        return count(new LambdaQueryWrapper<ReturnCode>()
                .eq(ReturnCode::getReturnCode, returnCode)
                .eq(ReturnCode::getState, 1)) > 0;
    }
}
