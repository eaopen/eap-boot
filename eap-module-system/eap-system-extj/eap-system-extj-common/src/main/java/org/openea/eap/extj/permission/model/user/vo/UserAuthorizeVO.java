package org.openea.eap.extj.permission.model.user.vo;

import lombok.Builder;
import lombok.Data;
import org.openea.eap.extj.permission.model.user.mod.UserAuthorizeModel;

import java.util.List;

/**
 *
 *
 */
@Data
@Builder
public class UserAuthorizeVO {
    private List<UserAuthorizeModel> button;
    private List<UserAuthorizeModel> column;
    private List<UserAuthorizeModel> module;
    private List<UserAuthorizeModel> resource;
    private List<UserAuthorizeModel> form;
}
