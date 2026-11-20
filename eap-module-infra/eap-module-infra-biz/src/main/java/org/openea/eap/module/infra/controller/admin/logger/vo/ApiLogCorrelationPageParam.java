package org.openea.eap.module.infra.controller.admin.logger.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.openea.eap.framework.common.pojo.PageParam;

@Data
public abstract class ApiLogCorrelationPageParam extends PageParam {

    @Schema(description = "客户端 IP，精确匹配")
    private String userIp;

    @Schema(description = "链路追踪编号，精确匹配")
    private String traceId;
}
