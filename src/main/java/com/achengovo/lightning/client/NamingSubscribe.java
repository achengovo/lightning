package com.achengovo.lightning.client;

import com.alibaba.nacos.api.exception.NacosException;

public interface NamingSubscribe {
    /**
     * 订阅服务
     * @throws NacosException 异常
     */
    void subscribe() throws NacosException;
}
