package org.openea.eap.module.bpm.framework.flowable.core.candidate.strategy;

import org.openea.eap.framework.common.util.string.StrUtils;
import org.openea.eap.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import org.openea.eap.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import org.openea.eap.module.system.api.dept.PostApi;
import org.openea.eap.module.system.api.user.AdminUserApi;
import org.openea.eap.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

import static org.openea.eap.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 岗位 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author kyle
 */
@Component
public class BpmTaskCandidatePostStrategy implements BpmTaskCandidateStrategy {

    @Resource
    private PostApi postApi;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.POST;
    }

    @Override
    public void validateParam(String param) {
        Set<Long> postIds = StrUtils.splitToLongSet(param);
        postApi.validPostList(postIds);
    }

    @Override
    public Set<Long> calculateUsers(DelegateExecution execution, String param) {
        Set<Long> postIds = StrUtils.splitToLongSet(param);
        List<AdminUserRespDTO> users = adminUserApi.getUserListByPostIds(postIds);
        return convertSet(users, AdminUserRespDTO::getId);
    }

}