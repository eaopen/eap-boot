package org.openea.eap.module.message.enums;

import org.openea.eap.framework.common.enums.RpcConstants;

public class ApiConstants {
    public static final String NAME = "system-server"; // 与现有服务保持一致，单体内直调
    public static final String PREFIX = RpcConstants.RPC_API_PREFIX + "/message";
    public static final String VERSION = "1.0.0";
}
