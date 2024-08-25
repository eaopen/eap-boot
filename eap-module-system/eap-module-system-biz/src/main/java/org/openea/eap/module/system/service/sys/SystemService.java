package org.openea.eap.module.system.service.sys;

import java.util.*;
import javax.validation.*;
import org.openea.eap.module.system.controller.admin.sys.vo.*;
import org.openea.eap.module.system.dal.dataobject.sys.SystemDO;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.common.pojo.PageParam;

/**
 * 系统应用 Service 接口
 *
 * @author eap
 */
public interface SystemService {

    /**
     * 创建系统应用
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSystem(@Valid SystemSaveReqVO createReqVO);

    /**
     * 更新系统应用
     *
     * @param updateReqVO 更新信息
     */
    void updateSystem(@Valid SystemSaveReqVO updateReqVO);

    /**
     * 删除系统应用
     *
     * @param id 编号
     */
    void deleteSystem(Long id);

    /**
     * 获得系统应用
     *
     * @param id 编号
     * @return 系统应用
     */
    SystemDO getSystem(Long id);

    /**
     * 获得系统应用分页
     *
     * @param pageReqVO 分页查询
     * @return 系统应用分页
     */
    PageResult<SystemDO> getSystemPage(SystemPageReqVO pageReqVO);

}