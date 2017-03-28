package webdriver.utils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import webdriver.BaseEntity;
import webdriver.Browser;
import webdriver.CommonFunctions;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/** Perform utils for Http requests
 */
public class HttpUtils extends BaseEntity{


    private static final int ERROR_CODE = 400;
    private static final String ENCODING = "UTF-8";
    private DefaultHttpClient httpClient;
    private static String response;
    SSLContext ctx;
    /**
     * For SSL connections only
     * @param url
     */
    public HttpUtils(String url) {
        acceptAllCertificates(url);
    }

    /**
     * For http connections
     */
    public HttpUtils() {
    }

    private static class DefaultTrustManager implements X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
			// not necessary yet
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
            // not necessary yet
		}
    }


	private void acceptAllCerts(String urlSite) throws KeyManagementException, NoSuchAlgorithmException, IOException{
    	// configure the SSLContext with a TrustManager
        ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
        SSLContext.setDefault(ctx);
        URL url = new URL(urlSite);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
        });
        logger.info(conn.getResponseCode());
        conn.disconnect();
    }
	
    /**
     * Accept all certificates from particular domain url
     * @param url
     */
    public void acceptAllCertificates(String url){
    	try {
			acceptAllCerts(url);
		} catch (Exception e) {
			logger.info(this, e);
		}
    }


	/** Return response
	 * @return response
	 */
	public static String getResponse(){
		return response;
	}

	private DefaultHttpClient getHttpClient(){
		return httpClient;
	}

	/** Execute POST request
	 * @param url url
	 * @param formparams params
	 * @param cookieStore cookies
	 * @param logName text of logs
	 * @return httpClient
	 */
	public DefaultHttpClient executePostRequest(String url, List<NameValuePair> formparams, CookieStore cookieStore, String logName){
		for(int i = 0; i <= 2; i++){
			try{
				info("post request to url: " + url);
				HttpPost httpPost = new HttpPost(url);
			    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, ENCODING);
			    httpPost.setEntity(entity);
			    httpClient = getClient();
			    if(cookieStore != null){
			    	httpClient.setCookieStore(cookieStore);
			    }
			    HttpResponse httpResponse = httpClient.execute(httpPost);
			    response = EntityUtils.toString(httpResponse.getEntity(),ENCODING);
			    int result = httpResponse.getStatusLine().getStatusCode();
		    	if(result < ERROR_CODE){
		    		info("Success: " + logName);
			    	info("Title: " + CommonFunctions.regexGetMatchGroup(response, "<title>([\\s\\S]*)</title>", 1));
			    }else{
			    	info(result+"");
			    	warn("Failed: " + logName);
			    }
			    return httpClient;
			} catch (Exception e) {
				logger.debug(this, e);
			}
		}
		return null;
	}

	
	/** Execute POST request
	 * @param url url
	 * @param formparams params
	 * @param logName текст лога
	 * @return cookies from response
	 */
	public DefaultHttpClient executePostRequest(String url, List<NameValuePair> formparams, String logName){
		return executePostRequest(url,formparams,getClient().getCookieStore(),logName);
	}
	
	/** Execute POST request
	 * @param url url
	 * @param formparams params
	 * @return client
	 */
	public DefaultHttpClient executePostRequest(String url, List<NameValuePair> formparams){
		return executePostRequest(url,formparams,getClient().getCookieStore(),"POST request to URL: " + url);
	}
	
	
	/** Execute POST request
	 * @param url url
	 * @param strParameters параметры
	 * @return client
	 */
	public DefaultHttpClient executePostRequest(String url, String strParameters){
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		String[] pairs = strParameters.split("&");
		for (String pair : pairs) {
			String key = pair.split("=")[0];
			String value = pair.split("=")[1];
			formparams.add(new BasicNameValuePair(key, value));
		}
		return executePostRequest(url,formparams,getClient().getCookieStore(),"POST request to URL: " + url);
	}

	@SuppressWarnings("deprecation")
	public DefaultHttpClient getClient() {
		if (httpClient!=null){
			return httpClient;
		}
		if (ctx==null) {
			httpClient = new DefaultHttpClient();
			return httpClient;
		}
		SSLSocketFactory sf = new SSLSocketFactory(ctx);
		Scheme httpsScheme = new Scheme("https", sf, 443);
		Scheme httpScheme = new Scheme("http",PlainSocketFactory.getSocketFactory(), 80);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(httpsScheme);
		schemeRegistry.register(httpScheme);
		ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);
		httpClient = new DefaultHttpClient(cm);
		return httpClient;
	}

	/**
	 * Get String by regexp from response
	 * @param regexp
	 * @param group
	 * @return
	 */
	public String extractStringViaRegExpFromResponse(String regexp,int group) {
		return CommonFunctions.regexGetMatchGroup(response,regexp,group);
	}
	
	
	/** Execute GET request
	 * @param url url
	 * @param cookieStore cookies
	 * @param silent text of logs
	 * @return httpClient
	 */
	public DefaultHttpClient executeGetRequest(String url, CookieStore cookieStore, boolean silent){
		for(int i = 0; i <=2; i++){
			try{
				httpClient = getClient();
				if (!silent) {
					info("get request to url: " + url);
				}
				HttpGet httpGet = new HttpGet(url);
				if(cookieStore != null){
			    	httpClient.setCookieStore(cookieStore);
			    }
			    HttpResponse httpResponse = httpClient.execute(httpGet);
			    response = EntityUtils.toString(httpResponse.getEntity() ,Charset.forName(ENCODING));
			    int result = httpResponse.getStatusLine().getStatusCode();
		    	logResult(result, silent);
			    return httpClient;
			}catch (Exception e) {
				logger.debug(this, e);
			}
		}
		return null;
	}

    private void logResult(int responseCode, boolean isSilent) {
        if(responseCode < ERROR_CODE){
            if (!isSilent) {
                info("Success: " + responseCode);
                info("Title: " + CommonFunctions.regexGetMatchGroup(response, "<title>([\\s\\S]*)</title>", 1));
            }
        }else{
            if (!isSilent) {
                info("Code response:" + responseCode);
                warn("Failed: " + isSilent);
            }
        }
    }

	
	/** Execute GET request
	 * @param url url
	 * @param silent text of logs
	 * @return cookies from response
	 */
	public DefaultHttpClient executeGetRequest(String url, boolean silent){
		return executeGetRequest(url,getClient().getCookieStore(),silent);
	}

	/** Execute GET request
	 * @param url url
	 * @return cookies from response
	 */
	public DefaultHttpClient executeGetRequest(String url){
		return executeGetRequest(url,getClient().getCookieStore(),false);
	}
	
	/** Execute GET request
	 * @param url url
	 * @return cookies from response
	 */
	public DefaultHttpClient executeGetRequestWithBasicAuthorization(String url,String[] userPass){
		Credentials credentials = new UsernamePasswordCredentials(userPass[0],userPass[1]);
		getClient().getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),credentials);
		return executeGetRequest(url,getClient().getCookieStore(),false);
	}
	
	/**
	 * List of Cookies in selenium format
	 * @return list
	 */
	public List<org.openqa.selenium.Cookie> getCookies(){
		List<Cookie> cookies = getHttpClient().getCookieStore().getCookies();
		List<org.openqa.selenium.Cookie> seleniumCookies = new ArrayList<org.openqa.selenium.Cookie>();
		for (Cookie cookie : cookies) {
			org.openqa.selenium.Cookie seleniumCookie = new org.openqa.selenium.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(),cookie.getPath(), cookie.getExpiryDate());
			seleniumCookies.add(seleniumCookie);
		}
		return seleniumCookies; 
	}
	
	
	@Override
	protected String formatLogMsg(String message) {
		return message;
	}

	/**
	 * Set cookies to http client
	 * @param cookies
	 */
	public void setCookies(Set<org.openqa.selenium.Cookie> cookies) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 100);
		Date date = calendar.getTime();
		for (org.openqa.selenium.Cookie cookie : cookies) {
			BasicClientCookie httpCookie = new BasicClientCookie(cookie.getName(),cookie.getValue());
			httpCookie.setVersion(0);
			httpCookie.setDomain(cookie.getDomain());
			httpCookie.setPath(cookie.getPath());
			httpCookie.setExpiryDate(date);
			getClient().getCookieStore().addCookie(httpCookie);
		}
	}

	/**
	 * Get browser with synched cookies
	 * @return
	 */
	public Browser getBrowser(String url) {
		Browser browser = getBrowser();
		browser.navigate(url);
		browser.setCookies(getCookies());
		browser.navigate(url);
		return browser;
	}
}
