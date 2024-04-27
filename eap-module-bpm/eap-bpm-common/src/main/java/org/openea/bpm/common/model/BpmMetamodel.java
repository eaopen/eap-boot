package org.openea.bpm.common.model;

import lombok.Data;

@Data
public class BpmMetamodel {

    private String id;
    private String name;
    private String description;

    private Boolean enabled;

    private String engine;  // activity, flowable, simple, quick

    private String bpmDs;
    private String bizDs;

    private String defineTable;
    private String instanceTable;
    private String taskTable;
    private String taskHisTable;

}
