package org.openea.eap.extj.onlinedev.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.openea.eap.extj.engine.model.flowengine.FlowModel;

import java.util.List;
import java.util.Map;
@Data
@Schema(defaultValue = "功能数据创建表单")
public class VisualdevModelDataCrForm extends FlowModel {
    @Schema(name = "数据内容")
    private String data;
    @Schema(name = "状态")
    private String status;
    @Schema(name = "流程候选人列表")
    private Map<String, List<String>> candidateList;
    @Schema(name = "流程紧急度")
    private Integer flowUrgent = 1;
}
