package com.my.project.http.client;

import com.my.project.http.client.util.JsonUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class HttpUploadClient {

    /**
     * socks proxy address attribute name
     */
    private static final String SOCKS_ADDRESS = "socks.address";

    private int timeout;
    private String proxyHost;
    private Integer proxyPort;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public HttpUploadClient(int timeout, String proxyHost, Integer proxyPort) {
        this.timeout = timeout;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public Map<String, Object> upload(String target, Map<String, String> headers, Map<String, Object> requestParam) throws IOException {
        CloseableHttpClient httpClient;
        try {
            httpClient = httpClient(this.timeout);
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException("create http client error", e);
        }
        Map<String, Object> response = new HashMap<>();
        try {
            // build request parameters
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            for (Map.Entry<String, Object> param : requestParam.entrySet()) {
                String name = param.getKey();
                Object value = param.getValue();
                if (name != null && !"".equals(name.trim()) && value != null) {
                    if (value instanceof File) {
                        entityBuilder.addPart(name, new FileBody((File) value));
                    } else {
                        entityBuilder.addPart(name, new StringBody(JsonUtils.toJsonString(value), ContentType.APPLICATION_JSON));
                    }
                }
            }
            HttpEntity reqEntity = entityBuilder.build();
            HttpPost httpPost = new HttpPost(target);
            httpPost.setEntity(reqEntity);
            // build request headers
            for (Map.Entry<String, String> header : headers.entrySet()) {
                String name = header.getKey();
                String value = header.getValue();
                if (name != null && !"".equals(name.trim())) {
                    httpPost.setHeader(name, value);
                }
            }
            // do upload
            HttpClientContext context = HttpClientContext.create();
            if (this.proxyHost != null && this.proxyPort != null && this.proxyPort != null && this.proxyPort > 0 && this.proxyPort < 65535) {
                context.setAttribute(SOCKS_ADDRESS, new InetSocketAddress(this.proxyHost, this.proxyPort));
            }
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost, context);
            try {
                String responseBody = new BasicResponseHandler().handleResponse(httpResponse);
                if (responseBody != null) {
                    response = JsonUtils.jsonToMap(responseBody, String.class, Object.class);
                }
            } finally {
                httpResponse.close();
            }
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
        return response;
    }

    /**
     * Create HTTP Client
     *
     * @return HTTP Client
     */
    private CloseableHttpClient httpClient(int timeout) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(TrustAllStrategy.INSTANCE).build();
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new InnerPlainConnectionSocketFactory())
                .register("https", new InnerSSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(reg);
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(timeout)
                        .setConnectTimeout(timeout)
                        .setSocketTimeout(timeout).build())
                .build();
    }

    /**
     * Create Socket connection with SOCKS5 Proxy
     */
    private static class InnerPlainConnectionSocketFactory extends PlainConnectionSocketFactory {
        @Override
        public Socket createSocket(final HttpContext context) throws IOException {
            InetSocketAddress socksProxy = (InetSocketAddress) context.getAttribute(SOCKS_ADDRESS);
            if (socksProxy != null) {
                Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksProxy);
                return new Socket(proxy);
            } else {
                return super.createSocket(context);
            }
        }

        @Override
        public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
            InetSocketAddress socksProxy = (InetSocketAddress) context.getAttribute(SOCKS_ADDRESS);
            if (socksProxy != null) {
                remoteAddress = InetSocketAddress.createUnresolved(host.getHostName(), host.getPort());
            }
            return super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
        }
    }

    /**
     * Create SSL Socket connection with SOCKS5 Proxy
     */
    private static class InnerSSLConnectionSocketFactory extends SSLConnectionSocketFactory {
        public InnerSSLConnectionSocketFactory(SSLContext sslContext, HostnameVerifier hostnameVerifier) {
            super(sslContext, hostnameVerifier);
        }

        @Override
        public Socket createSocket(final HttpContext context) throws IOException {
            InetSocketAddress socksProxy = (InetSocketAddress) context.getAttribute(SOCKS_ADDRESS);
            if (socksProxy != null) {
                Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksProxy);
                return new Socket(proxy);
            } else {
                return super.createSocket(context);
            }
        }

        @Override
        public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
            InetSocketAddress socksProxy = (InetSocketAddress) context.getAttribute(SOCKS_ADDRESS);
            if (socksProxy != null) {
                remoteAddress = InetSocketAddress.createUnresolved(host.getHostName(), host.getPort());
            }
            return super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
        }
    }

}
