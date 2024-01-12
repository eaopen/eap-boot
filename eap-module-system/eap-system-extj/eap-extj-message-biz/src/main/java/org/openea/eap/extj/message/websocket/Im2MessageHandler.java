package org.openea.eap.extj.message.websocket;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.extj.base.PageModel;
import org.openea.eap.extj.message.entity.ImContentEntity;
import org.openea.eap.extj.message.entity.MessageEntity;
import org.openea.eap.extj.message.model.ImUnreadNumModel;
import org.openea.eap.extj.message.model.message.PaginationMessage;
import org.openea.eap.extj.message.model.websocket.onconnettion.OnConnectionModel;
import org.openea.eap.extj.message.model.websocket.receivemessage.ReceiveMessageModel;
import org.openea.eap.extj.message.model.websocket.savafile.ImageMessageModel;
import org.openea.eap.extj.message.model.websocket.savafile.VoiceMessageModel;
import org.openea.eap.extj.message.model.websocket.savamessage.SavaMessageModel;
import org.openea.eap.extj.message.service.ImContentService;
import org.openea.eap.extj.message.service.MessageService;
import org.openea.eap.extj.message.util.ConnectionType;
import org.openea.eap.extj.message.util.MessageChannelType;
import org.openea.eap.extj.message.util.MessageParameterEnum;
import org.openea.eap.extj.message.util.SendMessageTypeEnum;
import org.openea.eap.extj.util.*;
import org.openea.eap.framework.common.enums.UserTypeEnum;
import org.openea.eap.framework.security.core.LoginUser;
import org.openea.eap.framework.websocket.core.sender.WebSocketMessageSender;
import org.openea.eap.framework.websocket.core.session.WebSocketSessionManager;
import org.openea.eap.framework.websocket.core.util.WebSocketFrameworkUtils;
import org.openea.eap.module.system.dal.dataobject.user.AdminUserDO;
import org.openea.eap.module.system.service.user.AdminUserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Im2MessageHandler  {

    @Resource
    private WebSocketSessionManager webSocketSessionManager;

    @Resource
    private WebSocketMessageSender webSocketMessageSender;

    @Resource
    private AdminUserService userService;

    ////////////////////////

    @Resource
    private ImContentService imContentService;

    @Resource
    private MessageService messageService;


    protected void sendMessage(WebSocketSession session, Object message){
        webSocketMessageSender.sendObject(session.getId(), "im2-message-receive", message);
    }
    protected void sendMessage(String userId, Object message){
        Collection<WebSocketSession> sessions = webSocketSessionManager.getSessionList(UserTypeEnum.ADMIN.getValue(), new Long(userId));
        if(sessions!=null && sessions.size()>0){
            sendMessage(sessions.iterator().next(), message);
        }
    }


    public void processMessage(String message, WebSocketSession session){
        log.debug("WS消息内容: {}, {}", session.getId(), message);
        JSONObject receivedMessage = JSONObject.parseObject(message);
        String receivedMethod = receivedMessage.getString(MessageParameterEnum.PARAMETER_METHOD.getValue());
        String receivedToken = receivedMessage.getString(MessageParameterEnum.PARAMETER_TOKEN.getValue());

        LoginUser loginUser = WebSocketFrameworkUtils.getLoginUser(session);
        String userId = ""+WebSocketFrameworkUtils.getLoginUserId(session);

        switch (receivedMethod) {
            case ConnectionType.CONNECTION_ONCONNECTION:
                //建立连接
                //暂不支持多端登录

                // todo 消息通知待改造
                List<ImUnreadNumModel> unreadNums = imContentService.getUnreadList(userId);
                int unreadNoticeCount = messageService.getUnreadNoticeCount(userId);
                int unreadMessageCount = messageService.getUnreadMessageCount(userId);
                int unreadSystemMessageCount = messageService.getUnreadSystemMessageCount(userId);
                int unreadScheduleCount = messageService.getUnreadCount(userId,4);
                MessageEntity noticeDefaultText = messageService.getInfoDefault(1);
                PaginationMessage pagination = new PaginationMessage();
                pagination.setCurrentPage(1);
                pagination.setPageSize(1);
                List<MessageEntity> list = messageService.getMessageList(pagination, pagination.getType(),userId);
                MessageEntity messageDefaultText = new MessageEntity();
                if(list.size()>0){
                    messageDefaultText = list.get(0);
                }
                String noticeText = noticeDefaultText.getTitle() != null ? noticeDefaultText.getTitle() : "";
                String messageText = messageDefaultText.getTitle() != null ? messageDefaultText.getTitle() : "";
                Long noticeTime = noticeDefaultText.getCreatorTime() != null ? noticeDefaultText.getCreatorTime().getTime() : 0;
                Long messageTime = messageDefaultText.getCreatorTime() != null ? messageDefaultText.getCreatorTime().getTime() : 0;

                //转model后上传到mq服务器上
                OnConnectionModel onConnectionModel = new OnConnectionModel();
                onConnectionModel.setMethod(MessageChannelType.CHANNEL_INITMESSAGE);
                //onConnectionModel.setOnlineUsers(onlineUsers);
                onConnectionModel.setUnreadNums(JsonUtil.listToJsonField(unreadNums));
                onConnectionModel.setUnreadNoticeCount(unreadNoticeCount);
//                    onConnectionModel.setNoticeDefaultText(noticeText);
                onConnectionModel.setUnreadMessageCount(unreadMessageCount);
                onConnectionModel.setUnreadSystemMessageCount(unreadSystemMessageCount);
                onConnectionModel.setUnreadScheduleCount(unreadScheduleCount);
                onConnectionModel.setMessageDefaultText(messageText);
                onConnectionModel.setMessageDefaultTime(messageTime);
                onConnectionModel.setUserId(userId);
                int total = unreadNoticeCount+unreadMessageCount+unreadSystemMessageCount+unreadScheduleCount;
                onConnectionModel.setUnreadTotalCount(total);
                sendMessage(session, onConnectionModel);

                // todo 暂不通知新用户上线
                //通知所有在线用户，有用户在线

                break;

            case ConnectionType.CONNECTION_SENDMESSAGE:
                //发送消息
                String toUserId = receivedMessage.getString(MessageParameterEnum.PARAMETER_TOUSERID.getValue());
                //text/voice/image
                String messageType = receivedMessage.getString(MessageParameterEnum.PARAMETER_MESSAGETYPE.getValue());
                String messageContent = receivedMessage.getString(MessageParameterEnum.PARAMETER_MESSAGECONTENT.getValue());

                String fileName = "";
                if (!SendMessageTypeEnum.MESSAGE_TEXT.getMessage().equals(messageType)) {
                    JSONObject object = JSONObject.parseObject(messageContent);
                    fileName = object.getString("name");
                }

                // get online user and check toUser
                // 暂不考虑多端同时登录问题
                //saveMessage
                if (SendMessageTypeEnum.MESSAGE_TEXT.getMessage().equals(messageType)) {
                    messageContent = XSSEscape.escape(messageContent);
                    imContentService.sendMessage(userId, toUserId, messageContent, messageType);
                } else if (SendMessageTypeEnum.MESSAGE_IMAGE.getMessage().equals(messageType)) {
                    JSONObject image = new JSONObject();
                    image.put("path", UploaderUtil.uploaderImg("/api/file/Image/IM/", fileName));
                    image.put("width", JSONObject.parseObject(messageContent).getString("width"));
                    image.put("height", JSONObject.parseObject(messageContent).getString("height"));
                    imContentService.sendMessage(userId, toUserId, image.toJSONString(), messageType);
                } else if (SendMessageTypeEnum.MESSAGE_VOICE.getMessage().equals(messageType)) {
                    JSONObject voice = new JSONObject();
                    voice.put("path", UploaderUtil.uploaderImg("/api/file/Image/IM/", fileName));
                    voice.put("length", JSONObject.parseObject(messageContent).getString("length"));
                    imContentService.sendMessage(userId, toUserId, voice.toJSONString(), messageType);
                }

                //组装model
                SavaMessageModel savaMessageModel = new SavaMessageModel();
                savaMessageModel.setMethod(MessageChannelType.CHANNEL_SENDMESSAGE);
                savaMessageModel.setUserId(userId);
                savaMessageModel.setToUserId(toUserId);
                savaMessageModel.setDateTime(DateUtil.getNowDate().getTime());

                // 获取用户信息
                AdminUserDO userInfo =userService.getUser(loginUser.getId());
                if(userInfo!=null){
                    //头像
                    savaMessageModel.setHeadIcon(UploaderUtil.uploaderImg(userInfo.getAvatar()));
                    //最新消息
                    savaMessageModel.setLatestDate(DateUtil.getNowDate().getTime());
                    //用户姓名
                    savaMessageModel.setRealName(userInfo.getNickname());
                    savaMessageModel.setAccount(userInfo.getUsername());
                }

                //对方的名称账号头像
                AdminUserDO toUser =userService.getUser(new Long(toUserId));
                savaMessageModel.setToAccount(toUser.getUsername());
                savaMessageModel.setToRealName(toUser.getNickname());
                savaMessageModel.setToHeadIcon(UploaderUtil.uploaderImg(toUser.getAvatar()));

                if (SendMessageTypeEnum.MESSAGE_TEXT.getMessage().equals(messageType)) {
                    savaMessageModel.setMessageType(messageType);
                    savaMessageModel.setToMessage(messageContent);
                } else if (SendMessageTypeEnum.MESSAGE_IMAGE.getMessage().equals(messageType)) {
                    //构建图片模型
                    ImageMessageModel messageModel = getImageModel(messageContent, UploaderUtil.uploaderImg("/api/file/Image/IM/", fileName));
                    savaMessageModel.setToMessage(messageModel);
                    savaMessageModel.setMessageType(messageType);
                } else if (SendMessageTypeEnum.MESSAGE_VOICE.getMessage().equals(messageType)) {
                    //构建语音模型
                    VoiceMessageModel messageModel = getVoiceMessageModel(messageContent, UploaderUtil.uploaderImg("/api/file/Image/IM/", fileName));
                    savaMessageModel.setMessageType(messageType);
                    savaMessageModel.setToMessage(messageModel);
                }
                // send message
                sendMessage(userId, savaMessageModel);
                //OnlineUserProvider.sendMessage(model, savaMessageModel);

                //接受消息
                ReceiveMessageModel receiveMessageModel = new ReceiveMessageModel();
                receiveMessageModel.setMethod(MessageChannelType.CHANNEL_RECEIVEMESSAGE);
                receiveMessageModel.setFormUserId(userId);
                receiveMessageModel.setDateTime(DateUtil.getNowDate().getTime());
                //头像
                receiveMessageModel.setHeadIcon(UploaderUtil.uploaderImg(userInfo.getAvatar()));
                //最新消息
                receiveMessageModel.setLatestDate(DateUtil.getNowDate().getTime());
                //用户姓名
                receiveMessageModel.setRealName(userInfo.getNickname());
                receiveMessageModel.setAccount(userInfo.getUsername());
                receiveMessageModel.setUserId(toUserId);
                // toUser
                if (SendMessageTypeEnum.MESSAGE_TEXT.getMessage().equals(messageType)) {
                    receiveMessageModel.setMessageType(messageType);
                    receiveMessageModel.setFormMessage(messageContent);
                } else if (SendMessageTypeEnum.MESSAGE_IMAGE.getMessage().equals(messageType)) {
                    //构建图片模型
                    ImageMessageModel messageModel = getImageModel(messageContent, UploaderUtil.uploaderImg("/api/file/Image/IM/", fileName));
                    receiveMessageModel.setMessageType(messageType);
                    receiveMessageModel.setFormMessage(messageModel);
                } else if (SendMessageTypeEnum.MESSAGE_VOICE.getMessage().equals(messageType)) {
                    //构建语音模型
                    VoiceMessageModel messageModel = getVoiceMessageModel(messageContent, UploaderUtil.uploaderImg("/api/file/Image/IM/", fileName));
                    receiveMessageModel.setMessageType(messageType);
                    receiveMessageModel.setFormMessage(messageModel);
                }
                // send message
                sendMessage(toUserId, receiveMessageModel);
                //OnlineUserProvider.sendMessage(toUser, receiveMessageModel);

                break;
            case "UpdateReadMessage":
                //更新已读
                String formUserId = receivedMessage.getString("formUserId");

                imContentService.readMessage(formUserId, userId);

                break;
            case "MessageList":
                //获取消息列表
                String sendUserId = receivedMessage.getString("toUserId");
                String receiveUserId = receivedMessage.getString("formUserId");
                PageModel pageModel = new PageModel();
                pageModel.setPage(receivedMessage.getInteger("currentPage"));
                pageModel.setRows(receivedMessage.getInteger("pageSize"));
                pageModel.setSord(receivedMessage.getString("sord"));
                pageModel.setKeyword(receivedMessage.getString("keyword"));
                List<ImContentEntity> data = imContentService.getMessageList(sendUserId, receiveUserId, pageModel).stream().sorted(Comparator.comparing(ImContentEntity::getSendTime)).collect(Collectors.toList());
                JSONObject object = new JSONObject();
                object.put("method", "messageList");
                object.put("list", JsonUtil.getListToJsonArray(data));
                JSONObject pagination2 = new JSONObject();
                pagination2.put("total", pageModel.getRecords());
                pagination2.put("currentPage", pageModel.getPage());
                pagination2.put("pageSize", receivedMessage.getInteger("pageSize"));
                object.put("pagination", pagination2);
                //OnlineUserProvider.sendMessage(session, object);
                sendMessage(session, object);
                break;
            default:
                break;
        }
    }

    /**
     * 判断是否为多租户
     *
     */
    private boolean isMultiTenancy() {
        return false;
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


}
