package com.achengovo.lightning.client.filter;

import com.achengovo.lightning.client.Client;
import com.achengovo.lightning.commons.message.RpcRequest;

import java.util.concurrent.ExecutionException;

/**
 * 拦截器
 */
public interface Filter {
    /**
     * 拦截方法
     * @param client 客户端
     * @param request 请求
     * @return 返回结果
     * @throws ExecutionException
     * @throws InterruptedException
     */
    Object filter(Client client, RpcRequest request) throws ExecutionException, InterruptedException;
}
