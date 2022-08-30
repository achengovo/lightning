package com.achengovo.lightning.client.loadbalance;

import com.achengovo.lightning.client.Client;

import java.util.List;
import java.util.Random;
/**
 * 随机负载均衡算法
 */
public class RandomLoadbalanceImpl implements LoadBalance{
    @Override
    public Client select(List<Client> clients) {
        for(Client client:clients){
            if(!client.isAvailable()){
                clients.remove(client);
            }
        }
        int random = new Random().nextInt(clients.size());
        Client client = clients.get(random);
        return client;
    }
}
