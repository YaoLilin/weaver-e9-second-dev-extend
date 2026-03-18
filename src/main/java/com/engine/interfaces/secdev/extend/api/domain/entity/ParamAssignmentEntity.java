package com.engine.interfaces.secdev.extend.api.domain.entity;

import lombok.Data;

/**
 * @author 姚礼林
 * @desc 赋值配置实体
 * @date 2026/1/27
 **/
@Data
public class ParamAssignmentEntity {
    private Integer id;
    private Integer assignmentMethod;
    private Integer assignmentIsMainTable;
    private Integer assignmentDetailTableNum;
    private String assignmentFieldName;
    private String assignmentValue;
}
