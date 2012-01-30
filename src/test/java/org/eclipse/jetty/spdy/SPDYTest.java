package org.eclipse.jetty.spdy;

import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.spdy.api.Session;
import org.eclipse.jetty.spdy.api.server.ServerSessionFrameListener;
import org.eclipse.jetty.spdy.nio.SPDYClient;
import org.eclipse.jetty.spdy.nio.SPDYServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.After;

public abstract class SPDYTest
{
    private Server server;
    private SPDYClient.Factory clientFactory;

    protected InetSocketAddress startServer(ServerSessionFrameListener listener) throws Exception
    {
        server = new Server();
        Connector connector = newSPDYServerConnector(listener);
        connector.setPort(47443);
        server.addConnector(connector);
        server.start();
        return new InetSocketAddress(connector.getLocalPort());
    }

    protected Connector newSPDYServerConnector(ServerSessionFrameListener listener)
    {
        return new SPDYServerConnector(listener);
    }

    protected Session startClient(InetSocketAddress socketAddress, Session.FrameListener frameListener) throws Exception
    {
        if (clientFactory == null)
        {
            QueuedThreadPool threadPool = new QueuedThreadPool();
            threadPool.setName(threadPool.getName() + "-client");
            clientFactory = newSPDYClientFactory(threadPool);
            clientFactory.start();
        }
        return clientFactory.newSPDYClient().connect(socketAddress, frameListener).get();
    }

    protected SPDYClient.Factory newSPDYClientFactory(ThreadPool threadPool)
    {
        return new SPDYClient.Factory(threadPool);
    }

    protected SslContextFactory newSslContextFactory()
    {
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath("src/test/resources/keystore.jks");
        sslContextFactory.setKeyStorePassword("storepwd");
        sslContextFactory.setTrustStore("src/test/resources/truststore.jks");
        sslContextFactory.setTrustStorePassword("storepwd");
        sslContextFactory.setProtocol("TLSv1");
        sslContextFactory.setIncludeProtocols("TLSv1");
        return sslContextFactory;
    }

    @After
    public void destroy() throws Exception
    {
        if (clientFactory != null)
        {
            clientFactory.stop();
            clientFactory.join();
        }
        if (server != null)
        {
            server.stop();
            server.join();
        }
    }
}
