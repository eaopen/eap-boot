package org.openea.eap.module.system.dal.dataobject.sys;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import org.openea.eap.framework.mybatis.core.dataobject.BaseDO;

/**
 * 系统应用 DO
 *
 * @author eap
 */
@TableName("sys_system")
@KeySequence("sys_system_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 系统编号
     */
    private String code;
    /**
     * 系统名称
     */
    private String fullName;
    /**
     * 系统图标
     */
    private String icon;
    /**
     * 是否主系统
     */
    private Integer isMain;

    /**
     * 依赖系统编号
     * (多个逗号分隔)
     */
    private String depSysCodes;
    /**
     * 扩展属性
     */
    private String propertyJson;
    /**
     * 描述或说明
     */
    private String description;
    /**
     * 导航图标
     */
    private String navigationIcon;
    /**
     * logo图标
     */
    private String workLogoIcon;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 系统状态
     */
    private Integer status;

}
