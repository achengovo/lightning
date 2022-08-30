package com.achengovo.lightning.client;

import com.achengovo.lightning.commons.message.RpcRequest;

import java.util.concurrent.ExecutionException;

public interface Client {
    /**
     * 发送请求
     * @param request
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    Object request(RpcRequest request) throws ExecutionException, InterruptedException;
    /**
     * 连接
     * @throws InterruptedException
     */
    void connect() throws InterruptedException;
    /**
     * 是否可用
     * @return
     */
    boolean isAvailable();
    /**
     * 获取权重
     * @return
     */
    int getWeight();
    /**
     * 获取服务端地址
     */
    String getHost();
    /**
     * 获取服务端端口
     */
    int getPort();
}
