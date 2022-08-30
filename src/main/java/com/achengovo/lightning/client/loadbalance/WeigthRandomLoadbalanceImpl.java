package com.achengovo.lightning.client.loadbalance;

import com.achengovo.lightning.client.Client;

import java.util.List;
import java.util.Random;

/**
 * 按权重的随机负载均衡算法
 */
public class WeigthRandomLoadbalanceImpl implements LoadBalance {
    @Override
    public Client select(List<Client> clients) {
        int weightSum = 0;

        for (Client client : clients) {
            if (client.isAvailable()) {
                weightSum += client.getWeight();
            }
        }
        int random = new Random().nextInt(weightSum);
        int weight = 0;
        for (Client client : clients) {
            if (client.isAvailable()) {
                weight += client.getWeight();
                if (weight > random) {
                    return client;
                }
            }

        }
        return null;
    }
}
