package com.engine.interfaces.secdev.extend.api.service.impl;

import com.engine.core.impl.Service;
import com.engine.interfaces.secdev.extend.api.cmd.GetInterfaceConfCmd;
import com.engine.interfaces.secdev.extend.api.cmd.InterfaceListCmd;
import com.engine.interfaces.secdev.extend.api.cmd.SaveOrUpdateInterfaceConfCmd;
import com.engine.interfaces.secdev.extend.api.cmd.SaveOrUpdateApiParamMappingCmd;
import com.engine.interfaces.secdev.extend.api.cmd.InterfaceParamMappingDetailCmd;
import com.engine.interfaces.secdev.extend.api.cmd.InterfaceParamMappingListCmd;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceConfDto;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceListItem;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceParamMappingDetail;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceParamMappingListItem;
import com.engine.interfaces.secdev.extend.api.domain.param.InterfaceConfParam;
import com.engine.interfaces.secdev.extend.api.domain.param.InterfaceParamMappingParam;
import com.customization.yll.common.web.modal.vo.PageResult;
import com.engine.interfaces.secdev.extend.api.service.InterfaceIntegrationService;

/**
 * 接口集成配置服务实现
 *
 * @author yaolilin
 * @date 2025/1/17
 */
public class InterfaceIntegrationServiceImpl extends Service implements InterfaceIntegrationService {

    @Override
    public boolean createInterfaceConf(InterfaceConfParam interfaceConfParam) {
        SaveOrUpdateInterfaceConfCmd cmd = new SaveOrUpdateInterfaceConfCmd(interfaceConfParam);
        if (commandExecutor == null) {
            cmd.execute(null);
        }
        return commandExecutor.execute(cmd);
    }

    @Override
    public PageResult<InterfaceListItem> getInterfaceList(Integer pageNo, Integer pageSize,
                                                          String name, String url, String apiId) {
        InterfaceListCmd cmd = new InterfaceListCmd(pageNo, pageSize, name, url, apiId);
        if (commandExecutor == null) {
            return cmd.execute(null);
        }
        return commandExecutor.execute(cmd);
    }

    @Override
    public InterfaceConfDto getInterfaceConf(Integer confId) {
        GetInterfaceConfCmd cmd = new GetInterfaceConfCmd(confId);
        if (commandExecutor == null) {
            return cmd.execute(null);
        }
        return commandExecutor.execute(cmd);
    }

    @Override
    public boolean saveApiParamMapping(InterfaceParamMappingParam mapperParam) {
        SaveOrUpdateApiParamMappingCmd cmd = new SaveOrUpdateApiParamMappingCmd(mapperParam);
        if (commandExecutor == null) {
            return cmd.execute(null);
        }
        return commandExecutor.execute(cmd);
    }

    @Override
    public PageResult<InterfaceParamMappingListItem> getApiParamMappingList(Integer pageNo, Integer pageSize) {
        InterfaceParamMappingListCmd cmd = new InterfaceParamMappingListCmd(pageNo, pageSize);
        if (commandExecutor == null) {
            return cmd.execute(null);
        }
        return commandExecutor.execute(cmd);
    }

    @Override
    public InterfaceParamMappingDetail getApiParamMappingDetail(Integer id) {
        InterfaceParamMappingDetailCmd cmd = new InterfaceParamMappingDetailCmd(id);
        if (commandExecutor == null) {
            return cmd.execute(null);
        }
        return commandExecutor.execute(cmd);
    }
}
