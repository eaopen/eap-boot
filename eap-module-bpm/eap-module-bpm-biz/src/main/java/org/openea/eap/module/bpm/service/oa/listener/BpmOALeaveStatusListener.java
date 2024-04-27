package org.openea.eap.module.bpm.service.oa.listener;

import org.openea.eap.module.bpm.event.BpmProcessInstanceStatusEvent;
import org.openea.eap.module.bpm.event.BpmProcessInstanceStatusEventListener;
import org.openea.eap.module.bpm.service.oa.BpmOALeaveService;
import org.openea.eap.module.bpm.service.oa.BpmOALeaveServiceImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * OA 请假单的结果的监听器实现类
 *
 */
@Component
public class BpmOALeaveStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private BpmOALeaveService leaveService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmOALeaveServiceImpl.PROCESS_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        leaveService.updateLeaveStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }

}
