package com.achengovo.lightning.client.loadbalance;

import com.achengovo.lightning.client.Client;

import java.util.List;
/**
 * 负载均衡算法
 */
public interface LoadBalance {
    /**
     * 选择一个可用的client
     * @param clients 可用的client列表
     * @return client
     */
    Client select(List<Client> clients);
}
