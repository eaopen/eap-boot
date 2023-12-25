package org.openea.eap.extj.base.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.openea.eap.extj.base.Pagination;

@Data
@Schema(name = "查询条件模型")
public class PaginationModel extends Pagination {
    @Schema(name = "查询条件json")
    private String queryJson;
    @Schema(name = "菜单id")
    private String menuId;
    @Schema(name = "关联字段")
    private String relationField;
    @Schema(name = "字段对象")
    private String columnOptions;
    @Schema(name = "数据类型")
    private String dataType;
    @Schema(name = "高级查询条件json")
    private String superQueryJson;


}
