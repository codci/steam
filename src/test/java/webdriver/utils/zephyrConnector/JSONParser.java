package webdriver.utils.zephyrConnector;

import com.jayway.jsonpath.JsonPath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import webdriver.Logger;

import java.util.Iterator;


public class JSONParser {

    private static final Logger logger = Logger.getInstance();

    public static String getJsonElement(String jsonTxtVar, String key) throws JSONException {
        Object obj;
        try {
            obj = JsonPath.read(jsonTxtVar, key);
            return (obj.toString());
        } catch (Exception e) {
            logger.debug("JSONParser.getJsonElement", e);
            throw new JSONException("Error parsing JSON using key. Key = " + key + "\n Exception : " + e.getMessage());
        }
    }


    public String parseJsonArray(String jsonTxt, String key, String value) throws JSONException {
        JSONArray jsonArr = null;
        JSONObject jsonObj = null;
        boolean found = false;
        try {
            jsonArr = new JSONArray(jsonTxt);
        } catch (Exception e) {
            logger.debug(this, e);
            throw new JSONException("JSON string is not a JSON array. Exception : " + e.getMessage());
        }

        for (int index = 0; index < jsonArr.length(); index++) {
            jsonObj = jsonArr.getJSONObject(index);
            if (jsonObj.get(key).toString().equalsIgnoreCase(value)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new JSONException("Object not found with key = " + key + "  and value = " + value);
        }
        return jsonObj.toString();
    }


    public String getObjectKeys(String jsonTxtVar, String keyTagName) throws JSONException {

        Iterator obj_keys = null;
        String key_list = "";
        JSONObject jsonObj = null;
        String json = getJsonElement(jsonTxtVar, keyTagName);
        try {
            jsonObj = new JSONObject(json);
        } catch (JSONException e) {
            logger.debug(this, e);
            throw new JSONException("Not a valid JSON object. Exception :" + e.getMessage());
        }
        try {
            obj_keys = jsonObj.keys();
        } catch (Exception e) {
            logger.debug(this, e);
            throw new JSONException("Error while retrieving object keys. Exception :" + e.getMessage());
        }
        while (obj_keys.hasNext()) {
            key_list += obj_keys.next().toString();
            if (obj_keys.hasNext()) {
                key_list += ",";
            }
        }
        return (key_list);
    }

}
