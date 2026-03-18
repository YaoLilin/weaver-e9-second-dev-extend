package com.engine.interfaces.secdev.extend.api.cmd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.RecordSetFactory;
import com.engine.core.interceptor.AbstractCommand;
import com.engine.core.interceptor.CommandContext;
import com.engine.interfaces.secdev.extend.api.dao.InterfaceConfDao;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceConfDto;
import com.engine.interfaces.secdev.extend.api.domain.enums.ParamType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yaolilin
 * @desc 获取接口配置信息
 * @date 2025/1/18
 **/
public class GetInterfaceConfCmd extends AbstractCommand<InterfaceConfDto> {
    private final InterfaceConfDao interfaceConfDao;
    private final Integer confId;
    private final IntegrationLog log = new IntegrationLog(this.getClass());

    public GetInterfaceConfCmd(Integer confId) {
        this.confId = confId;
        this.interfaceConfDao = new InterfaceConfDao(RecordSetFactory.instance());
    }

    @Override
    public InterfaceConfDto execute(CommandContext commandContext) {
        try {
            InterfaceConfDao.InterfaceConfRecord confRecord = interfaceConfDao.getConfById(confId);
            if (confRecord == null) {
                return null;
            }
            InterfaceConfDto conf = new InterfaceConfDto();
            conf.setId(confId);
            conf.setApiId(confRecord.getApiId());
            conf.setUrl(confRecord.getUrl());
            conf.setMethod(confRecord.getMethod());
            conf.setName(confRecord.getName());
            conf.setBodyType(confRecord.getBodyType());
            getParameters(conf, interfaceConfDao.listParamRecords(confId));
            removeRef(conf.getBodyParameters());
            return conf;
        } catch (Exception e) {
            log.error("执行发生异常", e);
            throw e;
        }
    }

    private void getParameters(InterfaceConfDto conf, List<InterfaceConfDao.InterfaceParamRecord> records) {
        Map<Integer, List<InterfaceConfDto.InterfaceParameter>> parametersMap = new HashMap<>(5);
        parametersMap.put(0, new ArrayList<>());
        parametersMap.put(1, new ArrayList<>());
        parametersMap.put(2, new ArrayList<>());
        parametersMap.put(3, new ArrayList<>());
        Map<String, InterfaceConfDto.InterfaceParameter> paramIdToParamMap = new HashMap<>(20);
        for (InterfaceConfDao.InterfaceParamRecord paramRecord : records) {
            InterfaceConfDto.InterfaceParameter parameter = new InterfaceConfDto.InterfaceParameter();
            parameter.setId(paramRecord.getParamId());
            parameter.setName(paramRecord.getName());
            parameter.setShowName(paramRecord.getShowName());
            parameter.setType(ParamType.fromValue(paramRecord.getType()));
            parameter.setRequired(paramRecord.getRequired());
            parameter.setParentId(paramRecord.getParentId());
            parameter.setChildren(null);
            paramIdToParamMap.put(paramRecord.getParamId(), parameter);

            if (StrUtil.isEmpty(paramRecord.getParentId()) && parametersMap.containsKey(paramRecord.getPosition())) {
                parametersMap.get(paramRecord.getPosition()).add(parameter);
            }
            // 添加子参数
            if (StrUtil.isNotEmpty(paramRecord.getParentId()) && paramIdToParamMap.containsKey(paramRecord.getParentId())) {
                addChildParam(paramIdToParamMap, paramRecord.getParentId(), parameter);
            }
        }
        conf.setQueryParameters(parametersMap.get(0));
        conf.setHeaderParameters(parametersMap.get(1));
        conf.setBodyParameters(parametersMap.get(2));
        conf.setReturnParameters(parametersMap.get(3));
    }

    private static void addChildParam(Map<String, InterfaceConfDto.InterfaceParameter> paramIdToParamMap,
                                      String parentId, InterfaceConfDto.InterfaceParameter parameter) {
        InterfaceConfDto.InterfaceParameter parentParameter = paramIdToParamMap.get(parentId);
        if (parentParameter.getChildren() == null) {
            parentParameter.setChildren(new ArrayList<>());
        }
        parentParameter.getChildren().add(parameter);
    }

    private void removeRef(List<InterfaceConfDto.InterfaceParameter> bodyParameter) {
        for (InterfaceConfDto.InterfaceParameter param : bodyParameter) {
            if (param.getChildren() != null) {
                List<InterfaceConfDto.InterfaceParameter> children = param.getChildren();
                List<InterfaceConfDto.InterfaceParameter> newChildren = new ArrayList<>(children.size());
                for (InterfaceConfDto.InterfaceParameter child : children) {
                    InterfaceConfDto.InterfaceParameter newParameter = copyParam(child);
                    if (CollUtil.isNotEmpty(newParameter.getChildren())) {
                        removeRef(newParameter.getChildren());
                    }
                    newChildren.add(newParameter);
                }
                param.setChildren(newChildren);
            }
        }
    }

    private static InterfaceConfDto.InterfaceParameter copyParam
            (InterfaceConfDto.InterfaceParameter parameter) {
        InterfaceConfDto.InterfaceParameter childParam = new InterfaceConfDto.InterfaceParameter();
        childParam.setId(parameter.getId());
        childParam.setName(parameter.getName());
        childParam.setShowName(parameter.getShowName());
        childParam.setType(parameter.getType());
        childParam.setRequired(parameter.getRequired());
        childParam.setParentId(parameter.getParentId());
        childParam.setChildren(parameter.getChildren());
        return childParam;
    }
}
