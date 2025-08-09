package org.openea.eap.module.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.module.message.dal.dataobject.MessageDO;
import org.openea.eap.module.message.dal.dataobject.MessageTaskDO;
import org.openea.eap.module.message.dal.mysql.MessageMapper;
import org.openea.eap.module.message.dal.mysql.MessageTaskMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.openea.eap.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/admin-api/message")
@Tag(name = "管理后台 - 消息查询")
public class MessageQueryController {

    @Resource
    private MessageMapper messageMapper;
    @Resource
    private MessageTaskMapper messageTaskMapper;

    @GetMapping("/{id}")
    @Operation(summary = "查询消息详情")
    public CommonResult<MessageDO> get(@PathVariable("id") Long id) {
        return success(messageMapper.selectById(id));
    }

    @GetMapping("/{id}/tasks")
    @Operation(summary = "查询消息的通道任务列表")
    public CommonResult<List<MessageTaskDO>> listTasks(@PathVariable("id") Long id) {
        List<MessageTaskDO> list = messageTaskMapper.selectList(new LambdaQueryWrapper<MessageTaskDO>()
                .eq(MessageTaskDO::getMessageId, id)
                .orderByDesc(MessageTaskDO::getId));
        return success(list);
    }
}
