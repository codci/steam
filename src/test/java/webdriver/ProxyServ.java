package webdriver;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.browsermob.core.har.Har;
import org.browsermob.core.har.HarEntry;
import org.browsermob.core.har.HarNameValuePair;
import org.browsermob.proxy.ProxyServer;


public class ProxyServ extends BaseEntity{
	
	private static final int ERROR_CODE = 400;
	private static volatile ProxyServer server;
	private static Har har;
	
	/** Getting Browsermob proxy server instance
	 * @return ProxyServer
	 */
	public static ProxyServer getProxyServer() {
		ProxyServer localInstance = server;
		if (localInstance == null) {
			synchronized (Browser.class) {
				localInstance = server;
				if (localInstance == null) {
					server = localInstance = new ProxyServer(4444);
				    try {
						server.start();
					} catch (Exception e1) {
						logger.debug("ProxyServ.getProxyServer", e1);
					}
				}
			}
		}
		return localInstance;	
	}
	
	
	/**
	 * Start to analyze traffic
	 */
	public static void startAnalyze() {
		getProxyServer().newHar("HAR_"+CommonFunctions.getTimestamp());
	}

	/**
	 * Assert traffic equals to expected
	 * @param harName harName
	 */
	public static void assertAnalyzedTraffic(String harName){
        har = getLastHar();
        List<HarEntry> arrayRequests = har.getLog().getEntries();
        for (HarEntry entry: arrayRequests) {
        	logger.info("--------------Request details-------------");
        	logger.info("TIME: "+entry.getTime());
        	logger.info("Method: "+entry.getRequest().getMethod());
        	logger.info("URL: "+entry.getRequest().getUrl()); 
        	logger.info("Http version: "+entry.getRequest().getHttpVersion()); 
        	logger.info("Headers Size: "+entry.getRequest().getHeadersSize());
        	logger.info("Body Size: "+entry.getRequest().getBodySize());
        	for(HarNameValuePair pair:  entry.getRequest().getHeaders()){
        		logger.info("Header parameter: " + pair.getName() + " = " + pair.getValue());
        	}
        	for(HarNameValuePair pair:  entry.getRequest().getQueryString()){
        		logger.info("Query Strings parameter: " + pair.getName() + " = " + pair.getValue());
        	}
        	logger.info("Body Size: "+entry.getRequest().getHeaders());
        	logger.info("------------------------------------------"); 
        }
        try {
			har.writeTo(new File(harName + ".har"));
		} catch (IOException e) {
            logger.debug("ProxyServ.assertAnalyzedTraffic", e);
			logger.warn(e.getMessage());
		}
        
	}
	

	/**
	 * Assert traffic response code less then 400
	 * @param harName harName
	 */
	public static void assertAnalyzedTrafficResponseCode(){
        har = getLastHar();
        try {
			List<HarEntry> arrayRequests = har.getLog().getEntries();
			for (HarEntry entry: arrayRequests) {
				if (entry.getResponse().getStatus()> ERROR_CODE){
					logger.warn("-------------------------Details of RESPONSE CODE failed request-----------------------------");
					logger.warn(String.format("Response code: %1$s; Response text: %2$s", entry.getResponse().getStatus(),entry.getResponse().getStatusText()));
					logger.warn(entry.getRequest().getMethod() + " " + entry.getRequest().getUrl());
					logger.warn("Mime-type: " + entry.getResponse().getContent().getMimeType());
					logger.warn("Content size: " + entry.getResponse().getContent().getSize() + "B");
					logger.warn("-------------------------------------------------------------------------------");
				}
			}
		} catch (Exception e) {
            logger.debug("ProxyServ.assertAnalyzedTrafficResponseCode", e);
            logger.warn(e.getMessage());
		}
	}


	/**
	 * Assert load time is less then x
	 * @param miliSeconds
	 */
	public static void assertAnalyzedTrafficLoadTime(long miliSeconds) {
        har = getLastHar();
        try {
			List<HarEntry> arrayRequests = har.getLog().getEntries();
			for (HarEntry entry: arrayRequests) {
				if (entry.getTime()>miliSeconds){
					logger.warn("-------------------------Details of LOAD-TIME failed request-----------------------------");
					logger.warn(String.format("Load time: %1$sms", entry.getTime()));
					logger.warn(entry.getRequest().getMethod() + " " + entry.getRequest().getUrl());
					logger.warn("Mime-type: " + entry.getResponse().getContent().getMimeType());
					logger.warn("Content size: " + entry.getResponse().getContent().getSize()+ "B");
					logger.warn("-------------------------------------------------------------------------------");
				}
			}
		} catch (Exception e) {
            logger.debug("ProxyServ.assertAnalyzedTrafficLoadTime", e);
            logger.warn(e.getMessage());
		}
	}
	
	/** Assert first buffer response time less then x
	 * @param timeLimitInMiliSeconds
	 */
	public static void assertAnalyzedTrafficFirstBufferResponse(int timeLimitInMiliSeconds) {
		//TODO: not implemented yet
	}

	/**
	 * Save to file
	 * @param harName
	 */
	static void saveToFile(String harName) {
		try {
			har.writeTo(new File("surefire-reports\\html\\Screenshots\\"+harName + ".har"));
			String formattedNameHar = String.format("<a href='Screenshots/%1$s.har'>Har file</a>", harName);
			logger.info(formattedNameHar);
		} catch (Exception e) {
            logger.debug("ProxyServ.saveToFile", e);
            logger.warn(e.getMessage());
		}
	}
	
	/**
	 * Get the last anylized har
	 * @return
	 */
	private static Har getLastHar() {
		if (har==null) {
			har = getProxyServer().getHar();
		}
		return har;
	}


	/**
	 * Stop server
	 */
	public static void stopProxyServer(){
		if (!Browser.getAnalyzeTraffic()){
			return;
		}
		try {
			getProxyServer().stop();
		} catch (Exception e) {
            logger.debug("ProxyServ.stopProxyServer", e);
            logger.warn(e.getMessage());
		}
		server = null;
	}


	@Override
	protected String formatLogMsg(String message) {
		return message;
	}

}
