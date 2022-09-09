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
    Map<String, Object> serviceInstances;

    public StartServer(Map<String, Object> serviceInstances,String namingUrl, Instance instance, String groupName) throws NacosException {
        this.serviceInstances = serviceInstances;
        this.naming = NamingFactory.createNamingService(namingUrl);
        instance.setInstanceId(instance.getClusterName()+"-"+instance.getServiceName()+"-"+instance.getIp()+":"+instance.getPort());
        this.instance = instance;
        this.groupName = groupName;
    }

    public void start() throws Exception {
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


