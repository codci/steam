package webdriver.utils.rest;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

/**
 * Created by zoltor on 16.05.15.
 */
public class RestParamList extends ArrayList<NameValuePair> {
    private static final long serialVersionUID = 7393519236225364397L;

    private boolean isBuilt = false;

    /**
     * Add element based on {@link BasicNameValuePair} to list
     * If current list building was finished - clear all list
     * @param paramName Pair param name for BasicNameValuePair
     * @param paramValue Pair param value for BasicNameValuePair
     * @return this
     */
    public RestParamList add(String paramName, String paramValue) {
        if (isBuilt) {
            isBuilt = false;
            this.clear();
        }
        NameValuePair paramPair;
        paramPair = new BasicNameValuePair(paramName, paramValue);
        super.add(paramPair);
        return this;
    }

    /**
     * Finish to build list. The next additions will be made after list erasing
     */
    public void build() {
        this.isBuilt = true;
    }

}
