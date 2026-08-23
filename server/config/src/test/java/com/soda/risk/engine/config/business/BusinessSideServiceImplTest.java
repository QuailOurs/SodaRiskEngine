package com.soda.risk.engine.config.business;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BusinessSideServiceImplTest {
    private BusinessSideMapper mapper;
    private BusinessSideServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(BusinessSideMapper.class);
        service = new BusinessSideServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
    }

    @Test
    void reportsDuplicateBusinessKey() {
        when(mapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        assertTrue(service.existsByKey("merchant", null));
    }

    @Test
    void reportsAvailableNameWhenExcludedRecordIsOnlyMatch() {
        when(mapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        assertFalse(service.existsByName("Merchant", 9L));
        verify(mapper).selectCount(any(Wrapper.class));
    }
}
