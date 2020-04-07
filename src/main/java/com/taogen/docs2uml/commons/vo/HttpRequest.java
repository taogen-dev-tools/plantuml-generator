package com.taogen.docs2uml.commons.vo;

import com.taogen.docs2uml.commons.constant.RequestMethod;
import lombok.Data;

import java.util.Map;

/**
 * @author Taogen
 */
@Data
public class HttpRequest {
    private String url;
    private RequestMethod requestMethod;
    private Map<String, String> headers;
    private String requestBody;

    public HttpRequest(String url){
        this.url = url;
    }

    public HttpRequest(String url, RequestMethod method){
        this.url = url;
        this.requestMethod = method;
    }
}
