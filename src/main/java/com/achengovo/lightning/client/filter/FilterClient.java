package com.achengovo.lightning.client.filter;

import com.achengovo.lightning.client.Client;
import com.achengovo.lightning.commons.message.RpcRequest;

import java.util.concurrent.ExecutionException;

/**
 * 拦截器客户端
 */
public class FilterClient implements Client {
    private Filter filter;
    private Client client;

    public FilterClient(Filter filter, Client client) {
        this.filter = filter;
        this.client = client;
    }

    @Override
    public Object request(RpcRequest request) throws ExecutionException, InterruptedException {
        return filter.filter(this.client, request);
    }

    @Override
    public void connect() throws InterruptedException {
        client.connect();
    }

    @Override
    public boolean isAvailable() {
        return client.isAvailable();
    }

    @Override
    public int getWeight() {
        return client.getWeight();
    }

    @Override
    public String getHost() {
        return client.getHost();
    }

    @Override
    public int getPort() {
        return client.getPort();
    }
}
