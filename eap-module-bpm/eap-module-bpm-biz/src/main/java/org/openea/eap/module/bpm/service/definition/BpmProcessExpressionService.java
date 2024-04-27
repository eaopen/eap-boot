package org.openea.eap.module.bpm.service.definition;

import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.module.bpm.controller.admin.definition.vo.expression.BpmProcessExpressionPageReqVO;
import org.openea.eap.module.bpm.controller.admin.definition.vo.expression.BpmProcessExpressionSaveReqVO;
import org.openea.eap.module.bpm.dal.dataobject.definition.BpmProcessExpressionDO;

import javax.validation.Valid;

/**
 * BPM 流程表达式 Service 接口
 *
 */
public interface BpmProcessExpressionService {

    /**
     * 创建流程表达式
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProcessExpression(@Valid BpmProcessExpressionSaveReqVO createReqVO);

    /**
     * 更新流程表达式
     *
     * @param updateReqVO 更新信息
     */
    void updateProcessExpression(@Valid BpmProcessExpressionSaveReqVO updateReqVO);

    /**
     * 删除流程表达式
     *
     * @param id 编号
     */
    void deleteProcessExpression(Long id);

    /**
     * 获得流程表达式
     *
     * @param id 编号
     * @return 流程表达式
     */
    BpmProcessExpressionDO getProcessExpression(Long id);

    /**
     * 获得流程表达式分页
     *
     * @param pageReqVO 分页查询
     * @return 流程表达式分页
     */
    PageResult<BpmProcessExpressionDO> getProcessExpressionPage(BpmProcessExpressionPageReqVO pageReqVO);

}
