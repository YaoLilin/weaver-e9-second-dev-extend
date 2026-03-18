package com.engine.interfaces.secdev.extend.api.cmd;

import cn.hutool.core.collection.CollUtil;
import com.customization.yll.common.RecordSetFactory;
import com.engine.core.interceptor.AbstractCommand;
import com.engine.core.interceptor.CommandContext;
import com.engine.interfaces.secdev.extend.api.dao.InterfaceConfDao;
import com.engine.interfaces.secdev.extend.api.domain.param.InterfaceConfParam;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * @author yaolilin
 * @desc 插入接口配置信息到建模
 * @date 2025/1/17
 **/
public class SaveOrUpdateInterfaceConfCmd extends AbstractCommand<Boolean> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final InterfaceConfParam interfaceConfParam;
    private final InterfaceConfDao interfaceConfDao;

    public SaveOrUpdateInterfaceConfCmd(InterfaceConfParam interfaceConfParam) {
        this.interfaceConfParam = interfaceConfParam;
        this.interfaceConfDao = new InterfaceConfDao(RecordSetFactory.instance());
    }

    @Override
    public Boolean execute(CommandContext commandContext) {
        try {
            int apiConfId = saveOrUpdateApiConf(interfaceConfParam);
            interfaceConfDao.deleteParameters(apiConfId);
            insertParameters(apiConfId, interfaceConfParam.getQueryParameters(), 1, null);
            insertParameters(apiConfId, interfaceConfParam.getHeaderParameters(), 1, null);
            insertParameters(apiConfId, interfaceConfParam.getBodyParameters(), 2, null);
            insertParameters(apiConfId, interfaceConfParam.getReturnParameters(), 3, null);
            return true;
        } catch (Exception e) {
            log.info("执行插入接口配置信息到数据库出错", e);
            return false;
        }
    }

    private int saveOrUpdateApiConf(InterfaceConfParam param) {
        Optional<Integer> idOp = interfaceConfDao.findConfIdByApiId(param.getApiId());
        if (idOp.isPresent()) {
            int id = idOp.get();
            interfaceConfDao.updateConf(id, param);
            return id;
        }
        return interfaceConfDao.insertConf(param);
    }

    private void insertParameters(int apiConfId, List<InterfaceConfParam.InterfaceParameter> parameters,
                                  int position, String parentId) {
        for (InterfaceConfParam.InterfaceParameter param : parameters) {
            interfaceConfDao.insertParameter(apiConfId, position, parentId, param);
            if (CollUtil.isNotEmpty(param.getChildren())) {
                insertParameters(apiConfId, param.getChildren(), position, param.getId());
            }
        }
    }
}
