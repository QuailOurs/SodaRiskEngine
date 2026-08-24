package com.soda.risk.engine.core.thirdparty;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ThirdPartyServiceFactoryTest {

    @Test
    void delegatesToTheFirstSupportingAdapterAndReturnsEmptyForUnknownTypes() {
        ThirdPartyServiceAdapter adapter = mock(ThirdPartyServiceAdapter.class);
        when(adapter.supports("model")).thenReturn(true);
        when(adapter.query(Map.of("input", 1))).thenReturn(Map.of("score", 88));
        ThirdPartyServiceFactory factory = new ThirdPartyServiceFactory(List.of(adapter));

        assertThat(factory.query("model", Map.of("input", 1))).containsEntry("score", 88);
        assertThat(factory.query("missing", Map.of())).isEmpty();
        verify(adapter).query(Map.of("input", 1));
    }
}
