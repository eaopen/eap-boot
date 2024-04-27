package org.openea.eap.module.bpm.framework.flowable.core.candidate.strategy;

import org.openea.eap.framework.test.core.ut.BaseMockitoUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.Set;

import static org.openea.eap.framework.common.util.collection.SetUtils.asSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BpmTaskCandidateUserStrategyTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmTaskCandidateUserStrategy strategy;

    @Test
    public void testCalculateUsers() {
        // 准备参数
        String param = "1,2";

        // 调用
        Set<Long> results = strategy.calculateUsers(null, param);
        // 断言
        assertEquals(asSet(1L, 2L), results);
    }

}
