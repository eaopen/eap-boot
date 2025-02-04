package org.openea.eap.module.system.framework.rpc.config;

import org.openea.eap.module.infra.api.config.ConfigApi;
import org.openea.eap.module.infra.api.file.FileApi;
import org.openea.eap.module.infra.api.websocket.WebSocketSenderApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@EnableFeignClients(clients = {FileApi.class, WebSocketSenderApi.class, ConfigApi.class})
public class RpcConfiguration {
}
