package com.engine.interfaces.secdev.extend.workflow.domain.dto;

import com.engine.interfaces.secdev.extend.workflow.domain.vo.FormField;
import lombok.Data;

import java.util.List;

/**
 * @author 姚礼林
 * @desc todo
 * @date 2026/1/14
 **/
@Data
public class DetailField {
    private Integer detailNum;
    private List<FormField> fields;
}
