package webdriver.utils.rest;


import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import webdriver.BaseEntity;

import java.io.*;

/**
 * Rest client
 */
public class RestClient extends BaseEntity {

    private final String apiUrl;
    private final RequestMethod requestMethod;
    private final RestParamList params;

    private int responseCode = -1;

    /**
     * Main constructor
     * @param apiUrl Full url to api which should be called
     * @param requestMethod Request method {@link RequestMethod}
     * @param params Request params {@link RestParamList}
     */
    public RestClient(String apiUrl, RequestMethod requestMethod, RestParamList params) {
        this.apiUrl = apiUrl;
        this.requestMethod = requestMethod;
        this.params = params;
    }

    /**
     * Send request with params was set through contructor
     * @return String with response body or empty string if none response was get (or errors occured)
     */
    public String doRequest() {
        logger.debug("======== REST TRACE INFO START ========");
        logger.debug("Url: " + apiUrl);
        logger.debug("Request Params: " + params);
        logger.debug("Request Method: " + requestMethod);
        HttpClientBuilder builder = HttpClientBuilder.create();
        CloseableHttpClient client = builder.build();
        HttpUriRequest request;
        HttpGet getRequest;
        HttpPost postRequest;
        CloseableHttpResponse response;
        switch (requestMethod) {
            case GET:
            case DELETE:
                getRequest = new HttpGet(apiUrl);
                request = getRequest;
                break;
            default:
                postRequest = new HttpPost(apiUrl);
                try {
                    postRequest.setEntity(new UrlEncodedFormEntity(params));
                } catch (UnsupportedEncodingException e) {
                    logger.debug(this, e);
                }
                request = postRequest;
        }
        try {
            response = client.execute(request);
            responseCode = response.getStatusLine().getStatusCode();
            String stringContent = getBodyFromContent(response.getEntity().getContent());
            logger.debug("Response Body: " + stringContent);
            logger.debug("Response Code: " + responseCode);
            response.close();
            logger.debug("======== REST TRACE INFO END ========");
            return stringContent;
        } catch (Exception e) {
            logger.debug("Exception: ", e);
            logger.debug("======== REST TRACE INFO END ========");
            return "";
        }
    }

    /**
     * Get latest response code
     * @return Response code or -1 if none request was executed
     */
    public int getResponseCode() {
        return responseCode;
    }


    //////////////////
    // Private methods
    //////////////////

    /**
     * Read content from InputStream and return back
     * @param content Content from {@link HttpEntity#getContent}
     * @return String with content from InputStream
     * @throws IOException
     */
    private String getBodyFromContent(InputStream content) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(content));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        return sb.toString();
    }

    @Override
    protected String formatLogMsg(String message) {
        return "";
    }
}
