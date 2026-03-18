package com.engine.interfaces.secdev.extend.workflow.domain.vo;

import com.engine.interfaces.secdev.extend.api.domain.vo.AssignmentVo;
import lombok.Data;

import java.util.List;

/**
 * @author 姚礼林
 * @desc 流程Action参数配置VO（返回给前端）
 * @date 2026/1/10
 **/
@Data
public class WorkflowActionParamConfigVO {
    /**
     * 主键ID（数据库自增，保存时可为空）
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
     * 参数名（与前端组件字段名一致）
     */
    private String name;

    /**
     * 显示名称（与前端组件字段名一致）
     */
    private String showName;

    /**
     * 是否必需（boolean 类型）
     */
    private Boolean required;

    /**
     * 参数类型（Integer 类型，返回枚举的 value 值，与前端组件字段名一致）
     * 0-字符串，1-数字，2-布尔，3-对象，4-数组
     */
    private Integer type;

    /**
     * 明细表序号（int 类型，记录明细表序号）
     */
    private Integer detailTable;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 赋值对象
     */
    private AssignmentVo assignment;

    /**
     * 子级参数列表（用于构建树形结构）
     */
    private List<WorkflowActionParamConfigVO> children;
    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 描述
     */
    private String desc;
}
