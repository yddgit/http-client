package com.my.project.http.client;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Base Test
 */
public class BaseTest {

    private static final byte[] KEYSTORE_SERVER_FILE = loadKeyStore("keystore_server");
    private static final String KEYSTORE_SERVER_PWD = "123456";
    /**
     * Port 0: the port will be assigned by OS
     */
    private static final String HTTP_BASE_URI = "http://localhost:0/test";
    /**
     * Port 0: the port will be assigned by OS
     */
    private static final String HTTPS_BASE_URI = "https://localhost:0/test";
    /**
     * HTTP Server
     */
    private HttpServer httpServer;
    /**
     * HTTPS Server
     */
    private HttpServer httpsServer;
    /**
     * Real HTTP base URI
     */
    protected String realHttpBaseURI = null;
    /**
     * Real HTTPS base URI
     */
    protected String realHttpsBaseURI = null;

    @Before
    public void startServer() {
        // http server
        final ResourceConfig rc = new ResourceConfig(MultiPartFeature.class).packages("com.my.project.http.client.stub");
        this.httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(HTTP_BASE_URI), rc);
        this.realHttpBaseURI = HTTP_BASE_URI.replace(":0", ":" + String.valueOf(this.httpServer.getListeners().iterator().next().getPort()));
        // https server
        SSLContextConfigurator sslContext = new SSLContextConfigurator();
        sslContext.setKeyStoreBytes(KEYSTORE_SERVER_FILE); // contains server keypair
        sslContext.setKeyStorePass(KEYSTORE_SERVER_PWD);
        this.httpsServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(HTTPS_BASE_URI), rc,
                true, new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(false));
        this.realHttpsBaseURI = HTTPS_BASE_URI.replace(":0", ":" + String.valueOf(this.httpsServer.getListeners().iterator().next().getPort()));
    }

    @After
    public void stopServer() {
        this.httpServer.shutdownNow();
    }

    /**
     * Get Upload URL (http)
     *
     * @return upload URL
     */
    protected String uploadUrlByHttp() {
        return this.realHttpBaseURI + "/api/upload";
    }

    /**
     * Get Upload URL (https)
     *
     * @return upload URL
     */
    protected String uploadUrlByHttps() {
        return this.realHttpsBaseURI + "/api/upload";
    }

    private static byte[] loadKeyStore(String keystore) {
        try {
            try (InputStream input = BaseTest.class.getClassLoader().getResourceAsStream(keystore)) {
                if (input != null) {
                    try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                        int len = 0;
                        byte[] buffer = new byte[1024];
                        while ((len = input.read(buffer, 0, buffer.length)) != -1) {
                            output.write(buffer, 0, len);
                        }
                        return output.toByteArray();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
