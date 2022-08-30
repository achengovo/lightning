package com.achengovo.lightning.client;

import com.achengovo.lightning.client.Client;
import com.achengovo.lightning.client.ClientPool;
import com.achengovo.lightning.client.filter.Filter;
import com.achengovo.lightning.client.loadbalance.LoadBalance;
import com.achengovo.lightning.commons.message.RpcRequest;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class Reference {
    private NamingService naming;
    private ClientPool clientPool;
    public Reference(String namingUrl,String serviceName,String groupName,List<Filter> filters) throws NacosException {
        this.naming=NamingFactory.createNamingService(namingUrl);
        this.clientPool=new ClientPool(naming,serviceName, groupName,filters);
        clientPool.subscribe();
    }
    public Reference(String namingUrl,String serviceName,String groupName) throws NacosException {
        this.naming=NamingFactory.createNamingService(namingUrl);
        this.clientPool=new ClientPool(naming,serviceName, groupName,new ArrayList<>());
        clientPool.subscribe();
    }
    /**
     * 创建代理对象
     * @param service 要代理的接口
     * @param loadBalance 负载均衡算法
     * @return Object
     * @throws NacosException
     * @throws InterruptedException
     */
    public Object createProxy(Class service, LoadBalance loadBalance) throws NacosException, InterruptedException {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                List<Client> clients = clientPool.getClients();
                Client client = loadBalance.select(clients);
                Object result=client.request(new RpcRequest(service.getName(), method.getName(), method.getParameterTypes(), args));
                return result;
            }
        });
    }
}
