package com.engine.interfaces.secdev.extend.workflow.domain.entity;

import lombok.Data;

/**
 * @author 姚礼林
 * @desc 流程Action参数配置实体（数据库实体）
 * @date 2026/1/10
 **/
@Data
public class WorkflowActionParamConfigEntity {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * Action标识
     */
    private String actionId;

    /**
     * 父级参数ID
     */
    private Integer parentId;

    /**
     * 参数名
     */
    private String paramName;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 是否必需（0-否，1-是）
     */
    private Integer required;

    /**
     * 参数类型（0-字符串，1-数字，2-布尔，3-对象，4-数组）
     */
    private Integer paramType;

    /**
     * 明细表序号
     */
    private Integer detailTable;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 赋值配置ID
     */
    private Integer assignmentId;
}
