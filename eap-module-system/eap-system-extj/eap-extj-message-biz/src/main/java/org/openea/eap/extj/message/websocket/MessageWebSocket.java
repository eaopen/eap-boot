package org.openea.eap.extj.message.websocket;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Strings;
import org.openea.eap.extj.base.UserInfo;
import org.openea.eap.extj.base.service.SysconfigService;
import org.openea.eap.extj.config.ConfigValueUtil;
import org.openea.eap.extj.message.model.websocket.savafile.ImageMessageModel;
import org.openea.eap.extj.message.model.websocket.savafile.VoiceMessageModel;
import org.openea.eap.extj.message.service.ImContentService;
import org.openea.eap.extj.message.service.MessageService;
import org.openea.eap.extj.message.util.OnlineUserModel;
import org.openea.eap.extj.message.util.OnlineUserProvider;
import org.openea.eap.extj.permission.service.UserService;
import org.openea.eap.extj.util.StringUtil;
import org.openea.eap.extj.util.UserProvider;
import org.openea.eap.extj.util.context.SpringContext;
import org.openea.eap.extj.util.data.DataSourceContextHolder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import javax.websocket.OnError;
import javax.websocket.Session;
import java.util.List;
import java.util.Map;

/**
 * 消息聊天
 *
 */
@Slf4j
@Component
//@ServerEndpoint(value = "/api/message/websocket/{token}")
//@ServerEndpoint("/websocket/message")
@Scope("prototype")
public class MessageWebSocket {

    private UserProvider userProvider;
    private ImContentService imContentService;
    private MessageService messageService;
    private ConfigValueUtil configValueUtil;
    private UserInfo userInfo;
    private UserService userApi;
    private SysconfigService sysConfigApi;


    /**
     * 判断是否为多租户
     *
     */
    private boolean isMultiTenancy() {
        if (configValueUtil.isMultiTenancy()) {
            //多租户需要切库
            if (StringUtil.isNotEmpty(userInfo.getTenantDbConnectionString())) {
                DataSourceContextHolder.setDatasource(userInfo.getTenantId(), userInfo.getTenantDbConnectionString(), userInfo.isAssignDataSource());
            }else{
                return false;
            }
        }
        return true;
    }

    /**
     * 构建图片消息模型
     *
     * @param messageContent
     * @param fileName
     * @return
     */
    private ImageMessageModel getImageModel(String messageContent, String fileName) {
        String width = JSONObject.parseObject(messageContent).getString("width");
        String height = JSONObject.parseObject(messageContent).getString("height");
        return new ImageMessageModel(width, height, fileName);
    }

    /**
     * 构建语音模型
     *
     * @param messageContent
     * @param fileName
     * @return
     */
    private VoiceMessageModel getVoiceMessageModel(String messageContent, String fileName) {
        String length = JSONObject.parseObject(messageContent).getString("length");
        return new VoiceMessageModel(length, fileName);
    }

    @OnError
    public void onError(Session session, Throwable error) {
//        OnlineUserModel user = OnlineUserProvider.getOnlineUserList().stream().filter(t -> t.getConnectionId().equals(session.getId())).findFirst().isPresent() ? OnlineUserProvider.getOnlineUserList().stream().filter(t -> t.getConnectionId().equals(session.getId())).findFirst().get() : null;
//        if (user != null) {
//            log.error("调用onError,租户：" + user.getTenantId() + ",用户：" + user.getUserId());
//        }
        try {
            //onClose(session);
        } catch (Exception e) {
//            log.error("发生error,调用onclose失败，session为：" + session);
        }
        if (error.getMessage() != null) {
            OnlineUserModel user = OnlineUserProvider.getOnlineUserList().stream().filter(t -> t.getConnectionId().equals(session.getId())).findFirst().isPresent() ? OnlineUserProvider.getOnlineUserList().stream().filter(t -> t.getConnectionId().equals(session.getId())).findFirst().get() : null;
            if(user != null){
                log.error("WS发生错误: {}, {}, {}, {}, {}", user.getTenantId(), user.getUserId(), session.getId(), error.getMessage(), user.getToken());
            }else{
                log.error("WS发生错误", error);
            }
        }
    }
    public static String getParam(@NotNull String key, Session session) {
        //TODO 目前只针对获取一个key的值，后期根据情况拓展多个 或者直接在onClose onOpen上获取参数？
        String value = null;
        Map<String, List<String>> parameters = session.getRequestParameterMap();
        if (MapUtil.isNotEmpty(parameters)) {
            value = parameters.get(key).get(0);
        } else {
            String queryString = session.getQueryString();
            if (!StrUtil.isEmpty(queryString)) {
                String[] params = Strings.split(queryString, '&');
                for (String paramPair : params) {
                    String[] nameValues = Strings.split(paramPair, '=');
                    if (key.equals(nameValues[0])) {
                        value = nameValues[1];
                    }
                }
            }
        }
        return value;
    }

    /**
     * 初始化
     */
    private void init() {
        messageService = SpringContext.getBean(MessageService.class);
        imContentService = SpringContext.getBean(ImContentService.class);
        configValueUtil = SpringContext.getBean(ConfigValueUtil.class);
        userProvider = SpringContext.getBean(UserProvider.class);
        userApi = SpringContext.getBean(UserService.class);
        sysConfigApi = SpringContext.getBean(SysconfigService.class);
//        uinPush = SpringContext.getBean(UinPush.class);
//        userDeviceService = SpringContext.getBean(UserDeviceService.class);
    }

}
