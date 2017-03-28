package webdriver.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import webdriver.Logger;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class WebClientDevWrapper {

    private static final Logger logger = Logger.getInstance();

    public static HttpClient wrapClient(HttpClient base) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    // not necessary yet
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    // not necessary yet
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            X509HostnameVerifier verifier = new X509HostnameVerifier() {

                @Override
                public void verify(String string, SSLSocket ssls) throws IOException {
                    // not necessary yet
                }

                @Override
                public void verify(String string, X509Certificate xc) throws SSLException {
                    // not necessary yet
                }

                @Override
                public void verify(String string, String[] strings, String[] strings1) throws SSLException {
                    // not necessary yet
                }

                @Override
                public boolean verify(String string, SSLSession ssls) {
                    return true;
                }
            };

            ctx.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ssf.setHostnameVerifier(verifier);
            ClientConnectionManager ccm = base.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
            return new DefaultHttpClient(ccm, base.getParams());
        } catch (Exception ex) {
            logger.info("WebClientDevWrapper.wrapClient", ex);
            return null;
        }

    }
}
