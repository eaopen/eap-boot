package org.openea.eap.module.infra.dal.dataobject.filetemplateconfig;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.openea.eap.framework.mybatis.core.dataobject.BaseDO;

/**
 * 文件模板配置 DO
 *
 * @author admin
 */
@TableName("infra_file_template_config")
@KeySequence("infra_file_template_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTemplateConfigDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 模板Key
     */
    private String tempKey;
    /**
     * 模板名称
     */
    private String tempName;
    /**
     * 模板类型
     */
    private Integer tempType;
    /**
     * 描述
     */
    private String description;
    /**
     * 关联文件
     */
    private Long fileId;
    /**
     * 系统服务接口
     */
    private String docService;
    /**
     * 配置Json
     */
    private String configJson;

}