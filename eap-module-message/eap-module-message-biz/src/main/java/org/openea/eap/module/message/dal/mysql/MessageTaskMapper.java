package org.openea.eap.module.message.dal.mysql;

import org.apache.ibatis.annotations.Mapper;
import org.openea.eap.framework.mybatis.core.mapper.BaseMapperX;
import org.openea.eap.module.message.dal.dataobject.MessageTaskDO;

@Mapper
public interface MessageTaskMapper extends BaseMapperX<MessageTaskDO> {
    /**
     * 统计剩余未完成（READY/PROCESSING）的任务数量
     */
    default long selectRemainingCount(Long messageId) {
        return selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MessageTaskDO>()
                .eq(MessageTaskDO::getMessageId, messageId)
                .in(MessageTaskDO::getStatus, 0, 1));
    }

    default long selectSuccessCount(Long messageId) {
        return selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MessageTaskDO>()
                .eq(MessageTaskDO::getMessageId, messageId)
                .eq(MessageTaskDO::getStatus, 2));
    }

    /**
     * 尝试占用任务：仅当当前状态为 READY(0) 时更新为 PROCESSING(1)
     *
     * @param id 任务ID
     * @return 是否占用成功
     */
    default boolean tryLockReady(Long id) {
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<MessageTaskDO> uw =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<MessageTaskDO>()
                        .eq(MessageTaskDO::getId, id)
                        .eq(MessageTaskDO::getStatus, 0)
                        .set(MessageTaskDO::getStatus, 1);
        // 使用 MyBatis-Plus 的 update(entity, wrapper) 形式；entity 传 null
        return update(null, uw) > 0;
    }
}
