package org.openea.eap.module.system.dal.dataobject.permission;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openea.eap.framework.mybatis.core.dataobject.BaseDO;

@Data
@EqualsAndHashCode(callSuper = true)
public class SystemDO extends BaseDO {


    public static final Long ID_MAIN = 0L;
    public static final Long ID_LOWCODE = 20L;
    public static final Long ID_BUSINESS = 30L;

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 名称
     */
    private String name;


}
