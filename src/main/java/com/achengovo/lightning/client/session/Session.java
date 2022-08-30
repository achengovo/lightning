package com.achengovo.lightning.client.session;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 缓存RPC调用结果
 */
public class Session {
    //缓存RPC调用结果
    private Map<Long, CompletableFuture> futureMap = new HashMap<>();
    /**
     * 添加Future
     * @param requestId 请求ID
     * @param future Future
     */
    public void addSession(Long requestId, CompletableFuture future){
        futureMap.put(requestId,future);
    }
    /**
     * 添加返回结果完成Future，并从futureMap删除Future
     * @param requestId 请求ID
     * @param result 返回结果
     */
    public void addResult(Long requestId,Object result){
        CompletableFuture future = futureMap.get(requestId);
        future.complete(result);
        futureMap.remove(requestId);
    }

    /**
     * 获取future个数
     * @return
     */
    public int getSize(){
        return futureMap.size();
    }
}
