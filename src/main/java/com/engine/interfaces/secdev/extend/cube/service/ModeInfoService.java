package com.engine.interfaces.secdev.extend.cube.service;

import com.engine.interfaces.secdev.extend.cube.domain.dto.ModeInfoItem;
import com.customization.yll.common.web.modal.vo.PageResult;

/**
 * @author 姚礼林
 * @desc 建模信息查询服务
 * @date 2026/1/23
 **/
public interface ModeInfoService {
    /**
     * 获取建模信息列表（支持条件与分页）
     *
     * @param id        模块ID
     * @param modeName  模块名称
     * @param tableName 表单名称
     * @param pageNo    页码
     * @param pageSize  每页数量
     * @return 建模信息分页结果
     */
    PageResult<ModeInfoItem> getModeInfoList(Integer id, String modeName, String tableName, Integer pageNo, Integer pageSize);
}
