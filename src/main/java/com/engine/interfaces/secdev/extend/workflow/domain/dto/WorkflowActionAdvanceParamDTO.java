package com.engine.interfaces.secdev.extend.workflow.domain.dto;

import com.engine.interfaces.secdev.extend.api.domain.dto.Assignment;
import com.engine.interfaces.secdev.extend.api.domain.enums.ParamType;
import lombok.Data;

import java.util.List;

/**
 * @author 姚礼林
 * @desc 保存流程Action参数配置DTO（用于接收前端保存请求）
 * @date 2026/1/10
 **/
@Data
public class WorkflowActionAdvanceParamDTO {
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
     * 参数类型（枚举类型）
     */
    private ParamType type;

    /**
     * 明细表序号（int 类型，记录明细表序号）
     */
    private Integer detailTable;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 赋值对象（确定的类型）
     */
    private Assignment assignment;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 描述
     */
    private String desc;

    /**
     * 子级参数列表（用于构建树形结构）
     */
    private List<WorkflowActionAdvanceParamDTO> children;
}
