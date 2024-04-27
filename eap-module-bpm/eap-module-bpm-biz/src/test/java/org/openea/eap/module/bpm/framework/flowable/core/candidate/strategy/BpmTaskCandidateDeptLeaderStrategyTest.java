package org.openea.eap.module.bpm.framework.flowable.core.candidate.strategy;

import org.openea.eap.framework.test.core.ut.BaseMockitoUnitTest;
import org.openea.eap.module.system.api.dept.DeptApi;
import org.openea.eap.module.system.api.dept.dto.DeptRespDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Set;

import static org.openea.eap.framework.common.util.collection.SetUtils.asSet;
import static org.openea.eap.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class BpmTaskCandidateDeptLeaderStrategyTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmTaskCandidateDeptLeaderStrategy strategy;

    @Mock
    private DeptApi deptApi;

    @Test
    public void testCalculateUsers() {
        // 准备参数
        String param = "1,2";
        // mock 方法
        DeptRespDTO dept1 = randomPojo(DeptRespDTO.class, o -> o.setLeaderUserId(11L));
        DeptRespDTO dept2 = randomPojo(DeptRespDTO.class, o -> o.setLeaderUserId(22L));
        when(deptApi.getDeptList(eq(asSet(1L, 2L)))).thenReturn(asList(dept1, dept2));

        // 调用
        Set<Long> results = strategy.calculateUsers(null, param);
        // 断言
        assertEquals(asSet(11L, 22L), results);
    }

}
