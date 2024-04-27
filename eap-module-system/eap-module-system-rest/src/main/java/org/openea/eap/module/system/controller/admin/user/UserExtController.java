package org.openea.eap.module.system.controller.admin.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.openea.eap.extj.base.vo.ListVO;
import org.openea.eap.extj.util.StringUtil;
import org.openea.eap.framework.common.enums.CommonStatusEnum;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.module.system.controller.admin.user.vo.user.ExtUserIdModel;
import org.openea.eap.module.system.controller.admin.user.vo.user.ExtUserListVo;
import org.openea.eap.module.system.controller.admin.user.vo.user.UserPageReqVO;
import org.openea.eap.module.system.convert.user.UserConvert;
import org.openea.eap.module.system.dal.dataobject.dept.DeptDO;
import org.openea.eap.module.system.dal.dataobject.user.AdminUserDO;
import org.openea.eap.module.system.service.dept.DeptService;
import org.openea.eap.module.system.service.user.AdminUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.openea.eap.framework.common.pojo.CommonResult.success;
import static org.openea.eap.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 用户扩展")
@RestController
@RequestMapping("/system/user")
@Validated
public class UserExtController {

    @Resource
    private AdminUserService userService;
    @Resource
    private DeptService deptService;


    @GetMapping("/ImUser")
    @Operation(summary = "IM通讯获取用户")
    @PreAuthorize("@ss.hasPermission('system:user:list')")
    public CommonResult<PageResult<ExtUserListVo>> getImUserPage(@Valid UserPageReqVO reqVO) {
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
        List<ExtUserListVo> userList = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(user -> {
            ExtUserListVo respVO =  userListVo(user);
            userList.add(respVO);
        });
        return success(new PageResult<>(userList, pageResult.getPagination()));
    }

    private ExtUserListVo userListVo(AdminUserDO user){
        if(user==null) return null;
        ExtUserListVo vo = UserConvert.INSTANCE.convertIm(user);
        if(user.getDeptId()!=null){
            DeptDO dept = deptService.getDept(user.getDeptId());
            if(dept!=null){
                vo.setDepartment(dept.getName());
            }
        }
        if(vo.getDepartment()==null){
            vo.setDepartment("");
        }
        if(StringUtil.isEmpty(vo.getFullName())){
            vo.setFullName(vo.getRealName());
        }
        return vo;
    }


    @Operation(summary = "获取用户基本信息")
    @Parameters({
            @Parameter(name = "userIdModel", description = "用户id", required = true)
    })
    @PostMapping("/getUserList")
    public CommonResult<ListVO<ExtUserListVo>> getUserList(@RequestBody ExtUserIdModel userIdModel){
        List<ExtUserListVo> list = new ArrayList<>();
        for (String userId : userIdModel.getUserId()) {
            if(!NumberUtil.isLong(userId)) continue;
            AdminUserDO user = userService.getUser(new Long(userId));
            // 排除无效用户
            if(user==null){
                continue;
            }
            ExtUserListVo vo = userListVo(user);
            list.add(vo);
        }
        ListVO<ExtUserListVo> listVO = new ListVO<>();
        listVO.setList(list);
        return success(listVO);
    }
}
