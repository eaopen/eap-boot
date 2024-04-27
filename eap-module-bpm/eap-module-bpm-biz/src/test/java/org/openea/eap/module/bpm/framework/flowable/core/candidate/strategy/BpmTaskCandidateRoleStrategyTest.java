package org.openea.eap.module.bpm.framework.flowable.core.candidate.strategy;

import org.openea.eap.framework.test.core.ut.BaseMockitoUnitTest;
import org.openea.eap.module.system.api.permission.PermissionApi;
import org.openea.eap.module.system.api.permission.RoleApi;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Set;

import static org.openea.eap.framework.common.util.collection.SetUtils.asSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class BpmTaskCandidateRoleStrategyTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmTaskCandidateRoleStrategy strategy;

    @Mock
    private RoleApi roleApi;
    @Mock
    private PermissionApi permissionApi;

    @Test
    public void testCalculateUsers() {
        // 准备参数
        String param = "1,2";
        // mock 方法
        when(permissionApi.getUserRoleIdListByRoleIds(eq(asSet(1L, 2L))))
            .thenReturn(asSet(11L, 22L));

        // 调用
        Set<Long> results = strategy.calculateUsers(null, param);
        // 断言
        assertEquals(asSet(11L, 22L), results);
    }

}
