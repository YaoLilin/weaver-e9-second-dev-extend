package com.engine.interfaces.secdev.extend.workflow.domain.dto;

import com.engine.interfaces.secdev.extend.workflow.domain.vo.FormField;
import lombok.Data;

import java.util.List;

/**
 * @author 姚礼林
 * @desc 表单字段信息
 * @date 2026/1/14
 **/
@Data
public class FormFieldsDTO {
    private List<FormField> mainFields;
    private List<DetailField> details;
}
