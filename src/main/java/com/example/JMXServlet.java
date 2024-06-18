package com.example;

import javax.management.*;
import javax.management.remote.*;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

@WebListener
public class JMXServlet implements ServletContextListener {
    private JMXConnectorServer connectorServer;
    private Registry registry;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // Create and register the MBean
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("com.example:type=Hello");
            Hello mbean = new Hello();
            mbs.registerMBean(mbean, name);

            // Create an RMI registry on port 1099
            registry = LocateRegistry.createRegistry(1099);

            // Create an RMI connector server
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
            Map<String, Object> env = new HashMap<>();
            env.put(JMXConnectorServer.AUTHENTICATOR, (JMXAuthenticator) credentials -> {
                // Simple authentication (replace with real authentication logic)
                if (credentials instanceof String[] && ((String[]) credentials).length == 2) {
                    String username = ((String[]) credentials)[0];
                    String password = ((String[]) credentials)[1];
                    if ("admin".equals(username) && "admin".equals(password)) {
                        return null; // No principal means successful authentication
                    }
                }
                throw new SecurityException("Authentication failed");
            });
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);

            // Start the RMI connector server
            connectorServer.start();
            System.out.println("JMX server is running on port 1099");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (connectorServer != null) {
                connectorServer.stop();
                System.out.println("JMX server stopped");
            }
            if (registry != null) {
                UnicastRemoteObject.unexportObject(registry, true);
                System.out.println("RMI registry stopped");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

