package org.therismos.authen;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author cpliu
 */
public class BlindTrustSSLFactory extends SSLSocketFactory {
    
    public static synchronized SocketFactory getDefault()
    {
      return INSTANCE;
    }

    private static final BlindTrustSSLFactory INSTANCE = new BlindTrustSSLFactory();
    private final SSLSocketFactory factory;

    private BlindTrustSSLFactory()
    {
      try
      {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[] { new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } }, null);
        factory = ctx.getSocketFactory();
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    }
    
    @Override
    public String[] getDefaultCipherSuites() {
        return factory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return factory.createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return factory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localhost, int localPort) throws IOException, UnknownHostException {
        return factory.createSocket(host, port, localhost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return factory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return factory.createSocket(address, port, localAddress, localPort);
    }
    
}
