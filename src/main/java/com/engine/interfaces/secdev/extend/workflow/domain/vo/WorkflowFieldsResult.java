package com.engine.interfaces.secdev.extend.workflow.domain.vo;

import com.engine.interfaces.secdev.extend.workflow.domain.dto.DetailField;
import lombok.Data;

import java.util.List;

/**
 * @author 姚礼林
 * @desc 流程字段信息，返回前端结构
 * @date 2026/1/14
 **/
@Data
public class WorkflowFieldsResult {
    private List<FormField> mainFields;
    private List<DetailField> details;
}
