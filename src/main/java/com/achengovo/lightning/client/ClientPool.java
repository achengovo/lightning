package com.achengovo.lightning.client;

import com.achengovo.lightning.client.filter.Filter;
import com.achengovo.lightning.client.filter.FilterClient;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/**
 * 客户端连接池
 */
public class ClientPool implements NamingSubscribe {
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    //保存client
    private volatile Map<String, Client> clientMap = new HashMap<>();
    //注册中心
    private NamingService naming;
    //服务名
    private String serviceName;
    //分组名
    private String groupName;
    List<Filter> filters;
    public ClientPool(NamingService naming, String serviceName, String groupName,List<Filter> filters) {
        this.naming = naming;
        this.serviceName = serviceName;
        this.groupName = groupName;
        this.filters=filters;
    }

    /**
     * 设置clientMap
     * @param clientMap clientMap
     */
    public void setClientMap(Map<String, Client> clientMap) {
        lock.lock();
        try {
            this.clientMap = clientMap;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
    /**
     * 获取clientMap
     * @return clientMap
     */
    public List<Client> getClients() {
        lock.lock();
        try {
            if (clientMap.size() == 0) {
                condition.await(5000, TimeUnit.MILLISECONDS);
            }
            return new ArrayList<>(clientMap.values());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return new ArrayList<>(clientMap.values());
    }

    /**
     * 订阅服务
     * @throws NacosException
     */
    @Override
    public void subscribe() throws NacosException {
        naming.subscribe(serviceName ,groupName, new EventListener() {
            @Override
            public void onEvent(Event event) {
                Map<String,Client> clientMapTemp = new HashMap<>();
                for (Instance instance : ((NamingEvent) event).getInstances()) {
                    if (instance.isHealthy()) {
                        Client client = new ClientImpl(instance.getIp(), instance.getPort(), (int) (instance.getWeight() * 100));
                        for(Filter filter:filters){
                            client = new FilterClient(filter, client);
                        }
                        try {
                            client.connect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        clientMapTemp.put(instance.getInstanceId(), client);
                    }
                }
                setClientMap(clientMapTemp);
            }
        });
    }
}
