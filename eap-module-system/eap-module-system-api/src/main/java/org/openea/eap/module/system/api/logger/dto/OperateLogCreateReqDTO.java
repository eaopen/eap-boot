package org.openea.eap.module.system.api.logger.dto;

import org.openea.eap.framework.common.enums.UserTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 操作日志创建 Request DTO
 */
@Data
public class OperateLogCreateReqDTO {

    /**
     * 链路追踪编号
     *
     * 一般来说，通过链路追踪编号，可以将访问日志，错误日志，链路追踪日志，logger 打印日志等，结合在一起，从而进行排错。
     */
    private String traceId;
    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 属性，或者 AdminUserDO 的 id 属性
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;
    /**
     * 用户类型
     *
     * 关联 {@link  UserTypeEnum}
     */
    @NotNull(message = "用户类型不能为空")
    private Integer userType;
    /**
     * 操作模块类型
     */
    @NotEmpty(message = "操作模块类型不能为空")
    private String type;
    /**
     * 操作名
     */
    @NotEmpty(message = "操作名不能为空")
    private String subType;
    /**
     * 操作模块业务编号
     */
    @NotNull(message = "操作模块业务编号不能为空")
    private Long bizId;
    /**
     * 操作内容，记录整个操作的明细
     * 例如说，修改编号为 1 的用户信息，将性别从男改成女，将姓名从芋道改成源码。
     */
    @NotEmpty(message = "操作内容不能为空")
    private String action;
    /**
     * 拓展字段，有些复杂的业务，需要记录一些字段 ( JSON 格式 )
     * 例如说，记录订单编号，{ orderId: "1"}
     */
    private String extra;

    /**
     * 请求方法名
     */
    @NotEmpty(message = "请求方法名不能为空")
    private String requestMethod;
    /**
     * 请求地址
     */
    @NotEmpty(message = "请求地址不能为空")
    private String requestUrl;
    /**
     * 用户 IP
     */
    @NotEmpty(message = "用户 IP 不能为空")
    private String userIp;
    /**
     * 浏览器 UA
     */
    @NotEmpty(message = "浏览器 UA 不能为空")
    private String userAgent;

    /**
     * Java 方法名
     */
    @NotEmpty(message = "Java 方法名不能为空")
    private String javaMethod;

    /**
     * Java 方法的参数
     */
    private String javaMethodArgs;

    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /**
     * 执行时长，单位：毫秒
     */
    @NotNull(message = "执行时长不能为空")
    private Integer duration;

    /**
     * 结果码
     */
    @NotNull(message = "结果码不能为空")
    private Integer resultCode;

    /**
     * 结果提示
     */
    private String resultMsg;

    /**
     * 结果数据
     */
    private String resultData;

}
