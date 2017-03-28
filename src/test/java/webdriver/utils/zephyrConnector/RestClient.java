package webdriver.utils.zephyrConnector;

import webdriver.Logger;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Scanner;

public class RestClient {

    private final String CHARSET = "UTF-8";
    private static final Logger logger = Logger.getInstance();

    public static void disableCertificateValidation() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // not necessary yet
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // not necessary yet
                    }
                }};

        // Ignore differences between given hostname and certificate hostname
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (Exception e) {
            logger.debug("RestClient.disableCertificateValidation", e);
        }
    }

    private URL createURL(String url) throws MalformedURLException {
        try {
            if (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")) {
                return new URL(url);
            } else {
                url = "http://" + url;
                return new URL(url);
            }
        } catch (MalformedURLException e) {
            logger.debug(this, e);
            throw e;
        }
    }

    protected synchronized String sendRequestOnce(String serverUrl, String method, String request_header, String requestBody) throws IOException {

        String response = null;
        HttpURLConnection connection = null;
        Scanner s;
        int response_code = 0;
        try {

            if (System.getProperty("proxy") != null) {
                String arr[] = System.getProperty("proxy").split(":");
                SocketAddress addr = new InetSocketAddress(arr[0], Integer.parseInt(arr[1]));
                Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);

                if (serverUrl.toLowerCase().startsWith("https://")) {
                    Object bb = createURL(serverUrl).openConnection(proxy);
                    if ("sun.net.www.protocol.http.HttpURLConnection".equals(bb.getClass().getCanonicalName())) {
                        connection = (sun.net.www.protocol.http.HttpURLConnection) bb;
                    } else {
                        connection = (HttpsURLConnection) bb;
                    }
                } else {
                    connection = (HttpURLConnection) createURL(serverUrl).openConnection(proxy);
                }
            } else {
                Object bb = createURL(serverUrl).openConnection();
                if ("sun.net.www.protocol.http.HttpURLConnection".equals(bb.getClass().getCanonicalName())) {
                    connection = (sun.net.www.protocol.http.HttpURLConnection) bb;
                } else {
                    connection = (HttpsURLConnection) bb;
                }
            }
            connection.setRequestMethod(method.toUpperCase());
            if (!"default".equalsIgnoreCase(request_header)) {
                String headers[] = request_header.split("&&");
                for (String pair : headers) {
                    String[] key_val = pair.split("=");
                    key_val[1] = pair.substring(pair.indexOf("=") + 1, pair.length());
                    connection.setRequestProperty(key_val[0], key_val[1]);
                }
            }
            connection.setRequestProperty("Accept-Charset", CHARSET);

            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                connection.setDoOutput(true);
                OutputStream output = connection.getOutputStream();
                output.write(requestBody.getBytes(CHARSET));
            }

            connection.connect();
            response_code = connection.getResponseCode();

            if (response_code != 200 && response_code != 201) {
                s = new Scanner(connection.getErrorStream());
            } else {
                s = new Scanner(connection.getInputStream());
            }
            s.useDelimiter("\\Z");
            response = s.next();
            connection.disconnect();

        } catch (Exception e) {
            logger.debug(this, e);
            connection.disconnect();
            throw new IOException("Exception : " + e.getMessage() + "\nResponse : " + response);
        }
        if (response_code != 200 && response_code != 201) {
            throw new IOException("Exception : Response Code : " + response_code + "\nResponse : " + response);
        }
        return response;
    }


    protected synchronized String sendRequest(String serverUrl, String method, String request_header, String requestBody) throws IOException {

        String s = null;

        try {
            s = sendRequestOnce(serverUrl, method, request_header, requestBody);
        } catch (Exception f) {
            logger.debug(this, f);
            throw new IOException(f);
        }
        if (s == null) {
            throw new IOException("Exception : Response to request(" + serverUrl + ") is null.");
        }

        return s;
    }
}