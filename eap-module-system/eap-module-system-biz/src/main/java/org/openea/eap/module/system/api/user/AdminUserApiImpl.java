package org.openea.eap.module.system.api.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import org.openea.eap.framework.common.util.object.BeanUtils;
import org.openea.eap.module.system.api.user.dto.AdminUserRespDTO;
import org.openea.eap.module.system.dal.dataobject.dept.DeptDO;
import org.openea.eap.module.system.dal.dataobject.user.AdminUserDO;
import org.openea.eap.module.system.service.dept.DeptService;
import org.openea.eap.module.system.service.user.AdminUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static org.openea.eap.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * Admin 用户 API 实现类
 *
 */
@Service
public class AdminUserApiImpl implements AdminUserApi {

    @Resource
    private AdminUserService userService;
    @Resource
    private DeptService deptService;

    @Override
    public AdminUserRespDTO getUser(Long id) {
        AdminUserDO user = userService.getUser(id);
        return BeanUtils.toBean(user, AdminUserRespDTO.class);
    }

    @Override
    public AdminUserRespDTO getUserByAccount(String account) {
        AdminUserDO user = userService.getUserByUsername(account);
        return BeanUtils.toBean(user, AdminUserRespDTO.class);
    }

    @Override
    public Set<Long> getSubordinateIds(Long id) {
        AdminUserDO user = userService.getUser(id);
        if (user == null) {
            return null;
        }

        Set<Long> subordinateIds = null; // 下属用户编号
        DeptDO dept = deptService.getDept(user.getDeptId());
        // TODO @puhui999：需要递归查询到子部门；并且要排除到自己噢。
        // TODO @puhui999：保持 if return 原则，这里其实要判断不等于，则返回 null；最好返回 空集合，上面也是
        if (ObjUtil.equal(dept.getLeaderUserId(), id)) { // 校验是否是该部门的负责人
            List<AdminUserDO> users = userService.getUserListByDeptIds(Collections.singletonList(dept.getId()));
            subordinateIds = convertSet(users, AdminUserDO::getId);
        }
        return subordinateIds;
    }

    @Override
    public List<AdminUserRespDTO> getUserListBySubordinate(Long id) {
        // 1.1 获取用户负责的部门
        AdminUserDO user = userService.getUser(id);
        if (user == null) {
            return Collections.emptyList();
        }
        ArrayList<Long> deptIds = new ArrayList<>();
        DeptDO dept = deptService.getDept(user.getDeptId());
        if (dept == null) {
            return Collections.emptyList();
        }
        if (ObjUtil.notEqual(dept.getLeaderUserId(), id)) { // 校验为负责人
            return Collections.emptyList();
        }
        deptIds.add(dept.getId());
        // 1.2 获取所有子部门
        List<DeptDO> childDeptList = deptService.getChildDeptList(dept.getId());
        if (CollUtil.isNotEmpty(childDeptList)) {
            deptIds.addAll(convertSet(childDeptList, DeptDO::getId));
        }

        // 2. 获取部门对应的用户信息
        List<AdminUserDO> users = userService.getUserListByDeptIds(deptIds);
        users.removeIf(item -> ObjUtil.equal(item.getId(), id)); // 排除自己
        return BeanUtils.toBean(users, AdminUserRespDTO.class);
    }

    @Override
    public List<AdminUserRespDTO> getUserList(Collection<Long> ids) {
        List<AdminUserDO> users = userService.getUserList(ids);
        return BeanUtils.toBean(users, AdminUserRespDTO.class);
    }

    @Override
    public List<AdminUserRespDTO> getUserListByDeptIds(Collection<Long> deptIds) {
        List<AdminUserDO> users = userService.getUserListByDeptIds(deptIds);
        return BeanUtils.toBean(users, AdminUserRespDTO.class);
    }

    @Override
    public List<AdminUserRespDTO> getUserListByPostIds(Collection<Long> postIds) {
        List<AdminUserDO> users = userService.getUserListByPostIds(postIds);
        return BeanUtils.toBean(users, AdminUserRespDTO.class);
    }

    @Override
    public void validateUserList(Collection<Long> ids) {
        userService.validateUserList(ids);
    }

}
