package com.engine.interfaces.secdev.extend.cube.web;

import com.customization.yll.common.web.WebExceptionHandler;
import com.customization.yll.common.web.exception.WebParamException;
import com.customization.yll.common.web.modal.vo.ApiResult;
import com.engine.common.util.ServiceUtil;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.FormFieldsDTO;
import com.engine.interfaces.secdev.extend.cube.domain.dto.ModeInfoItem;
import com.customization.yll.common.web.modal.vo.PageResult;
import com.engine.interfaces.secdev.extend.workflow.domain.vo.WorkflowFieldsResult;
import com.engine.interfaces.secdev.extend.cube.service.impl.ModeInfoServiceImpl;
import com.engine.interfaces.secdev.extend.workflow.service.impl.FormFieldServiceImpl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author 姚礼林
 * @desc 建模信息接口
 * @date 2026/1/23
 **/
public class ModeInfoAction {

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
    @Path("")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModeInfoList(@QueryParam("id") Integer id,
                                    @QueryParam("modeName") String modeName,
                                    @QueryParam("tableName") String tableName,
                                    @QueryParam("pageNo") Integer pageNo,
                                    @QueryParam("pageSize") Integer pageSize) {
        try {
            PageResult<ModeInfoItem> result = ServiceUtil.getService(ModeInfoServiceImpl.class)
                    .getModeInfoList(id, modeName, tableName, pageNo, pageSize);
            return Response.ok(ApiResult.success(result)).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }

    /**
     * 获取建模表单字段
     *
     * @param modeId 建模ID
     * @return 建模表单字段
     */
    @Path("/fields")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModeFields(@QueryParam("modeId") int modeId) {
        try {
            if (modeId <= 0) {
                throw new WebParamException.QueryParamException("modeId 参数必须大于0");
            }
            FormFieldsDTO dto = ServiceUtil.getService(FormFieldServiceImpl.class)
                    .getModeFields(modeId);
            if (dto == null) {
                throw new WebParamException.QueryParamException("找不到该建模的表单字段信息,modeId:" + modeId);
            }
            WorkflowFieldsResult result = new WorkflowFieldsResult();
            result.setMainFields(dto.getMainFields());
            result.setDetails(dto.getDetails());
            return Response.ok(ApiResult.success(result)).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }
}
