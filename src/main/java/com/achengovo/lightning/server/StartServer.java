package com.achengovo.lightning.server;

import com.achengovo.lightning.server.Server;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class StartServer {
    private NamingService naming;
    private Instance instance;
    private Server server;
    private String groupName;
    Object serviceImpl;

    public StartServer(String namingUrl, Instance instance, String groupName,Object serviceImpl) throws NacosException {
        this.naming = NamingFactory.createNamingService(namingUrl);
        this.instance = instance;
        this.groupName = groupName;
        this.serviceImpl=serviceImpl;
    }

    public void start() throws Exception {
        Map<String, Object> serviceInstances = new HashMap<>();
        serviceInstances.put(instance.getServiceName(), serviceImpl);
        this.server = new Server(serviceInstances, instance.getPort());
        server.start();
        naming.registerInstance(instance.getServiceName(), groupName, instance);
        command();
    }

    public void command() throws Exception {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String command = sc.next();
            if ("quit".equals(command)) {
                server.stop(naming, instance.getServiceName(), groupName, instance);
                break;
            }
        }
    }
}


