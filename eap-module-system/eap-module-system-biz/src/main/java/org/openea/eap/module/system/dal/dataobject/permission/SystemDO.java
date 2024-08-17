package org.openea.eap.module.system.dal.dataobject.permission;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openea.eap.framework.mybatis.core.dataobject.BaseDO;

@Data
@EqualsAndHashCode(callSuper = true)
public class SystemDO extends BaseDO {

    public static final Long ID_MAIN = 0L;

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     * eap, extn, obpm
     */
    private String type;

    // 是否需要配置菜单

    // 扩展属性
    private String configJson;

}
