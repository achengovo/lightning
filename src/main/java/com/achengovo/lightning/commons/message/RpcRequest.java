package com.achengovo.lightning.commons.message;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 请求消息
 */
public class RpcRequest implements Serializable {
    // 请求id
    private Long requestId;
    // 接口名称
    private String interfaceName;
    // 方法名称
    private String methodName;
    // 参数类型
    private Object parameterTypes[];
    // 参数值
    private Object[] parameters;

    /**
     * 构造函数
     * @return
     */
    public RpcRequest(String interfaceName, String methodName, Object[] parameterTypes, Object[] parameters) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameters = parameters;
        this.parameterTypes = parameterTypes;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Object[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Object[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "requestId='" + requestId + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }
}
