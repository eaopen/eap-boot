package org.openea.eap.extj.message.websocket;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.security.core.LoginUser;
import org.openea.eap.framework.websocket.core.listener.WebSocketMessageListener;
import org.openea.eap.framework.websocket.core.sender.WebSocketMessageSender;
import org.openea.eap.framework.websocket.core.util.WebSocketFrameworkUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;


@Component
@Slf4j
public class Im2WebSocketMessageListener implements WebSocketMessageListener<String> {

    @Resource
    private WebSocketMessageSender webSocketMessageSender;


    @Resource
    private Im2MessageHandler im2MessageHandler;



    /**
     * 处理消息
     *
     * @param session Session
     * @param message 消息
     */
    @Override
    public void onMessage(WebSocketSession session, String message) {
        LoginUser user = WebSocketFrameworkUtils.getLoginUser(session);
        try{
            im2MessageHandler.processMessage(message, session);
        }catch(Exception e){
            log.error("Im2 onMessage error:" +e.getMessage()+"\r\nmessage:\r\n"+ JSONUtil.toJsonPrettyStr(JSONUtil.parseObj(message)), e );
        }
    }

    /**
     * 获得消息类型
     *
     * @return 消息类型
     */
    @Override
    public String getType() {
        return "im2-message-send";
    }
}
