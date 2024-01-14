package org.openea.eap.extj.controller.admin.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.openea.eap.extj.base.ActionResult;
import org.openea.eap.extj.base.UserInfo;
import org.openea.eap.extj.base.entity.PrintLogEntity;
import org.openea.eap.extj.base.model.printlog.PrintLogInfo;
import org.openea.eap.extj.base.model.vo.PrintLogVO;
import org.openea.eap.extj.base.service.PrintLogService;
import org.openea.eap.extj.base.vo.PaginationVO;
import org.openea.eap.extj.permission.entity.UserEntity;
import org.openea.eap.extj.permission.service.UserService;
import org.openea.eap.extj.util.*;
import org.openea.eap.extj.base.model.printlog.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Tag(name = "打印模板日志", description = "PrintLogController")
@RestController
@RequestMapping("/vdsys/printLog")
public class PrintLogController {
    @Autowired
    private PrintLogService printLogService;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private UserService userService;

    /**
     * 获取列表
     *
     * @param page 分页模型
     * @return
     */
    @Operation(summary = "获取列表")
    @Parameters({
            @Parameter(name = "id", description = "打印模板ID", required = true)
    })
    @SaCheckPermission("system.printDev")
    @GetMapping("/{id}")
    public ActionResult<?> list(@PathVariable("id") String printId, PrintLogQuery page) {
        QueryWrapper<PrintLogEntity> wrapper = new QueryWrapper<>();
        String startTime = page.getStartTime();
        String endTime = page.getEndTime();

        if (!StringUtil.isEmpty(startTime) && !StringUtil.isEmpty(endTime)) {
            Date startTimes = DateUtil.stringToDate(DateUtil.daFormatYmd(Long.parseLong(startTime)) + " 00:00:00");
            Date endTimes = DateUtil.stringToDate(DateUtil.daFormatYmd(Long.parseLong(endTime)) + " 23:59:59");
            wrapper.lambda().ge(PrintLogEntity::getPrintTime, startTimes).le(PrintLogEntity::getPrintTime, endTimes);
        }

        if (StringUtils.isNoneBlank(printId)) {
            wrapper.lambda().eq(PrintLogEntity::getPrintId, printId);
        }
        if (StringUtils.isNoneBlank(page.getKeyword())) {
            wrapper.lambda().like(PrintLogEntity::getPrintTitle, page.getKeyword());
        }
        Page<PrintLogEntity> pageData = new Page<>(page.getCurrentPage(), page.getPageSize());
        IPage<PrintLogEntity> printIPage  = printLogService.page(pageData, wrapper);
        // 转化名称
        try {

            List<PrintLogEntity> records = printIPage.getRecords();
            List<String> collect = records.stream().map(PrintLogEntity::getPrintMan).filter(Objects::nonNull).collect(Collectors.toList());
            if (collect.size() > 0) {
                List<UserEntity> list = userService.getBaseMapper().selectBatchIds(collect);
                Map<String, String> map = list.stream().collect(Collectors.toMap(UserEntity::getId, UserEntity::getRealName));
                for (PrintLogEntity record : records) {
                    record.setPrintMan(map.get(record.getPrintMan()));
                }
                printIPage.setRecords(records);
            }


            page.setData(printIPage.getRecords(), printIPage.getTotal());
        } catch (Exception ignored) {

        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(page, PaginationVO.class);
        List<PrintLogVO> list = JsonUtil.getJsonToList(  printIPage.getRecords(), PrintLogVO.class);

        return ActionResult.page(list, paginationVO);
    }

    /**
     * 保存信息
     *
     * @param info 实体对象
     * @return
     */
    @Operation(summary = "保存信息")
    @Parameters({
            @Parameter(name = "info", description = "实体对象", required = true)
    })
    @SaCheckPermission("system.printDev")
    @PostMapping("save")
    public ActionResult<?> save(@RequestBody @Validated PrintLogInfo info) {
        PrintLogEntity printLogEntity = BeanUtil.copyProperties(info, PrintLogEntity.class);
        UserInfo userInfo = userProvider.get();

        printLogEntity.setId(RandomUtil.uuId());
        printLogEntity.setPrintTime(new Date());
        printLogEntity.setPrintMan(userInfo.getUserId());
        printLogService.save(printLogEntity);
        return ActionResult.success("保存成功");
    }


}
