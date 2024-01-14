package org.openea.eap.extj.extend.service.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.openea.eap.extj.base.ActionResult;
import org.openea.eap.extj.base.Pagination;
import org.openea.eap.extj.base.model.dataInterface.PaginationDataInterface;
import org.openea.eap.extj.base.service.SuperServiceImpl;
import org.openea.eap.extj.constant.MsgCode;
import org.openea.eap.extj.exception.DataException;
import org.openea.eap.extj.extend.entity.BillRuleEntity;
import org.openea.eap.extj.extend.mapper.BillRuleMapper;
import org.openea.eap.extj.extend.service.BillRuleService;
import org.openea.eap.extj.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BillRuleServiceImpl extends SuperServiceImpl<BillRuleMapper, BillRuleEntity> implements BillRuleService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<BillRuleEntity> getList(PaginationDataInterface pagination) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        QueryWrapper<BillRuleEntity> queryWrapper = new QueryWrapper<>();
        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            flag = true;
            queryWrapper.lambda().and(
                    t -> t.like(BillRuleEntity::getFullName, pagination.getKeyword())
                            .or().like(BillRuleEntity::getEnCode, pagination.getKeyword())
            );
        }
        if (!StringUtil.isEmpty(pagination.getCategoryId())) {
            flag = true;
            queryWrapper.lambda().and(
                    t -> t.like(BillRuleEntity::getCategory, pagination.getCategoryId())
            );
        }
        // 排序
        queryWrapper.lambda().orderByAsc(BillRuleEntity::getSortCode).orderByDesc(BillRuleEntity::getCreatorTime);
        if (flag) {
            queryWrapper.lambda().orderByDesc(BillRuleEntity::getLastModifyTime);
        }
        Page<BillRuleEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<BillRuleEntity> userPage = this.page(page, queryWrapper);
        return pagination.setData(userPage.getRecords(), page.getTotal());
    }

    @Override
    public List<BillRuleEntity> getList() {
        QueryWrapper<BillRuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BillRuleEntity::getEnabledMark, 1);
        // 排序
        queryWrapper.lambda().orderByAsc(BillRuleEntity::getSortCode).orderByDesc(BillRuleEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public BillRuleEntity getInfo(String id) {
        QueryWrapper<BillRuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BillRuleEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isExistByFullName(String fullName, String id) {
        QueryWrapper<BillRuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BillRuleEntity::getFullName, fullName);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(BillRuleEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public boolean isExistByEnCode(String enCode, String id) {
        QueryWrapper<BillRuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BillRuleEntity::getEnCode, enCode);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(BillRuleEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    @DSTransactional
    public String getNumber(String enCode) throws DataException {
        StringBuilder strNumber = new StringBuilder();
        QueryWrapper<BillRuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BillRuleEntity::getEnCode, enCode);
        BillRuleEntity entity = this.getOne(queryWrapper);
        if (entity != null) {
            Integer startNumber = Integer.parseInt(entity.getStartNumber());
            String dateFor = entity.getDateFormat();
            //处理隔天流水号归0
            if (entity.getOutputNumber() != null) {
                String serialDate;
                entity.setThisNumber(entity.getThisNumber() + 1);
                if (!"no".equals(dateFor)) {
                    String thisDate = DateUtil.dateNow(entity.getDateFormat());
                    serialDate = entity.getOutputNumber().substring((entity.getOutputNumber().length() - dateFor.length() - entity.getDigit()), (entity.getOutputNumber().length() - entity.getDigit()));
                    if (!serialDate.equals(thisDate)) {
                        entity.setThisNumber(0);
                    }
                }
            } else {
                entity.setThisNumber(0);
            }
            //拼接单据编码
            strNumber.append(entity.getPrefix());
            if (!"no".equals(dateFor)) {
                strNumber.append(DateUtil.dateNow(entity.getDateFormat()));
            }
            strNumber.append(PadUtil.padRight(String.valueOf((startNumber) + entity.getThisNumber()), entity.getDigit(), '0'));
            //更新流水号
            entity.setOutputNumber(strNumber.toString());
            this.updateById(entity);
        } else {
            throw new DataException("单据规则不存在");
        }
        return strNumber.toString();
    }

    @Override
    public void create(BillRuleEntity entity) {
        entity.setId(RandomUtil.uuId());
        entity.setCreatorUserId(userProvider.get().getUserId());
        this.save(entity);
    }

    @Override
    public boolean update(String id, BillRuleEntity entity) {
        entity.setId(id);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(userProvider.get().getUserId());
        return this.updateById(entity);
    }

    @Override
    public void delete(BillRuleEntity entity) {
        this.removeById(entity.getId());
    }

    @Override
    @DSTransactional
    public boolean first(String id) {
        boolean isOk = false;
        //获取要上移的那条数据的信息
        BillRuleEntity upEntity = this.getById(id);
        Long upSortCode = upEntity.getSortCode() == null ? 0 : upEntity.getSortCode();
        //查询上几条记录
        QueryWrapper<BillRuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .lt(BillRuleEntity::getSortCode, upSortCode)
                .orderByDesc(BillRuleEntity::getSortCode);
        List<BillRuleEntity> downEntity = this.list(queryWrapper);
        if (downEntity.size() > 0) {
            //交换两条记录的sort值
            Long temp = upEntity.getSortCode();
            upEntity.setSortCode(downEntity.get(0).getSortCode());
            downEntity.get(0).setSortCode(temp);
            this.updateById(downEntity.get(0));
            this.updateById(upEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    @DSTransactional
    public boolean next(String id) {
        boolean isOk = false;
        //获取要下移的那条数据的信息
        BillRuleEntity downEntity = this.getById(id);
        Long upSortCode = downEntity.getSortCode() == null ? 0 : downEntity.getSortCode();
        //查询下几条记录
        QueryWrapper<BillRuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .gt(BillRuleEntity::getSortCode, upSortCode)
                .orderByAsc(BillRuleEntity::getSortCode);
        List<BillRuleEntity> upEntity = this.list(queryWrapper);
        if (upEntity.size() > 0) {
            //交换两条记录的sort值
            Long temp = downEntity.getSortCode();
            downEntity.setSortCode(upEntity.get(0).getSortCode());
            upEntity.get(0).setSortCode(temp);
            this.updateById(upEntity.get(0));
            this.updateById(downEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    public String getBillNumber(String enCode, boolean isCache) throws DataException {
        String strNumber;
        String tenantId = !StringUtil.isEmpty(userProvider.get().getTenantId()) ? userProvider.get().getTenantId() : "";
        if (isCache) {
            String cacheKey = tenantId + userProvider.get().getUserId() + enCode;
            if (!redisUtil.exists(cacheKey)) {
                strNumber = this.getNumber(enCode);
                redisUtil.insert(cacheKey, strNumber);
            } else {
                strNumber = String.valueOf(redisUtil.getString(cacheKey));
            }
        } else {
            strNumber = this.getNumber(enCode);
        }
        return strNumber;
    }

    @Override
    public void useBillNumber(String enCode) {
        String cacheKey = userProvider.get().getTenantId() + userProvider.get().getUserId() + enCode;
        redisUtil.remove(cacheKey);
    }

    @Override
    public ActionResult ImportData(BillRuleEntity entity) throws DataException {
        if (entity != null) {
            if (isExistByFullName(entity.getFullName(), null)) {
                return ActionResult.fail(MsgCode.EXIST001.get());
            }
            if (isExistByEnCode(entity.getEnCode(), null)) {
                return ActionResult.fail(MsgCode.EXIST002.get());
            }
            try {
                this.saveOrUpdateIgnoreLogic(entity);
            } catch (Exception e) {
                throw new DataException(MsgCode.IMP003.get());
            }
            return ActionResult.success(MsgCode.IMP001.get());
        }
        return ActionResult.fail("导入数据格式不正确");
    }
    @Override
    public List<BillRuleEntity> getListByCategory(String id, Pagination pagination) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        QueryWrapper<BillRuleEntity> queryWrapper = new QueryWrapper<>();
        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            flag = true;
            queryWrapper.lambda().and(
                    t -> t.like(BillRuleEntity::getFullName, pagination.getKeyword())
                            .or().like(BillRuleEntity::getEnCode, pagination.getKeyword())
            );
        }
        if (!StringUtil.isEmpty(id)) {
            flag = true;
            queryWrapper.lambda().eq(BillRuleEntity::getCategory, id);
        }
        queryWrapper.lambda().eq(BillRuleEntity::getEnabledMark, 1);
        // 排序
        queryWrapper.lambda().orderByAsc(BillRuleEntity::getSortCode).orderByDesc(BillRuleEntity::getCreatorTime);
        if (flag) {
            queryWrapper.lambda().orderByDesc(BillRuleEntity::getLastModifyTime);
        }
        Page<BillRuleEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<BillRuleEntity> userPage = this.page(page, queryWrapper);
        return pagination.setData(userPage.getRecords(), page.getTotal());
    }
}
