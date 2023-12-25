package org.openea.eap.extj.permission.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.openea.eap.extj.base.mapper.SuperMapper;
import org.openea.eap.extj.base.model.base.SystemBaeModel;
import org.openea.eap.extj.base.model.button.ButtonModel;
import org.openea.eap.extj.base.model.column.ColumnModel;
import org.openea.eap.extj.base.model.form.ModuleFormModel;
import org.openea.eap.extj.base.model.module.ModuleModel;
import org.openea.eap.extj.base.model.resource.ResourceModel;
import org.openea.eap.extj.permission.entity.AuthorizeEntity;

import java.util.List;
@Mapper
public interface ExtjAuthorizeMapper extends SuperMapper<AuthorizeEntity> {


    List<ModuleModel> findModule(@Param("objectId") String objectId);

    List<ButtonModel> findButton(@Param("objectId") String objectId);

    List<ColumnModel> findColumn(@Param("objectId") String objectId);

    List<ResourceModel> findResource(@Param("objectId") String objectId);

    List<ModuleFormModel> findForms(@Param("objectId") String objectId);

    List<SystemBaeModel> findSystem(@Param("objectId") String objectId);

    List<ModuleModel> findModuleAdmin(@Param("mark") Integer mark);

    List<ButtonModel> findButtonAdmin(@Param("mark") Integer mark);

    List<ColumnModel> findColumnAdmin(@Param("mark") Integer mark);

    List<ResourceModel> findResourceAdmin(@Param("mark") Integer mark);

    List<ModuleFormModel> findFormsAdmin(@Param("mark") Integer mark);

    void saveBatch(@Param("values") String values);

    void savaAuth(AuthorizeEntity authorizeEntity);
}
