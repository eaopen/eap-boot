package org.openea.eap.module.system.controller.admin.user;

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.openea.eap.framework.common.enums.CommonStatusEnum;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.module.system.controller.admin.user.vo.user.ImUserListVo;
import org.openea.eap.module.system.controller.admin.user.vo.user.UserPageReqVO;
import org.openea.eap.module.system.convert.user.UserConvert;
import org.openea.eap.module.system.dal.dataobject.dept.DeptDO;
import org.openea.eap.module.system.dal.dataobject.user.AdminUserDO;
import org.openea.eap.module.system.service.dept.DeptService;
import org.openea.eap.module.system.service.user.AdminUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.openea.eap.framework.common.pojo.CommonResult.success;
import static org.openea.eap.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 用户IM")
@RestController
@RequestMapping("/system/user")
@Validated
public class UserImController {

    @Resource
    private AdminUserService userService;
    @Resource
    private DeptService deptService;

    @GetMapping("/ImUser")
    @Operation(summary = "IM通讯获取用户")
    @PreAuthorize("@ss.hasPermission('system:user:list')")
    public CommonResult<PageResult<ImUserListVo>> getImUserPage(@Valid UserPageReqVO reqVO) {
        // 获得用户分页列表
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        PageResult<AdminUserDO> pageResult = userService.getUserPage(reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal())); // 返回空
        }

        // 获得拼接需要的数据
        Collection<Long> deptIds = convertList(pageResult.getList(), AdminUserDO::getDeptId);
        Map<Long, DeptDO> deptMap = deptService.getDeptMap(deptIds);
        // 拼接结果返回
        List<ImUserListVo> userList = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(user -> {
            ImUserListVo respVO = UserConvert.INSTANCE.convertIm(user);
            DeptDO dept = deptService.getDept(user.getDeptId());
            if(dept!=null){
                respVO.setDepartment(dept.getName());
            }
            userList.add(respVO);
        });
        return success(new PageResult<>(userList, pageResult.getPagination()));
    }
}
