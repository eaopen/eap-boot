package org.openea.eap.module.message.service;

import org.openea.eap.module.message.api.dto.MessageSendReqDTO;
import org.openea.eap.module.message.api.dto.MessageSendRespDTO;

public interface MessageSendService {

    MessageSendRespDTO submit(MessageSendReqDTO reqDTO);
}
