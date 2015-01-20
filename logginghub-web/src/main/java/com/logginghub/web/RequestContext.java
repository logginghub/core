package com.logginghub.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestContext {

    private static ThreadLocal<RequestContext> threadContexts = new ThreadLocal<RequestContext>() {
        @Override protected RequestContext initialValue() {
            return new RequestContext();
        }
    };
    private String target;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public static RequestContext getRequestContext() {
        return threadContexts.get();
    }

    public void setup(String target, HttpServletRequest request, HttpServletResponse response) {
        this.target = target;
        this.request = request;
        this.response = response;
    }

    public Form getForm() {
        @SuppressWarnings("unchecked") Form form = new Form(request.getParameterMap());
        return form;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public String getTarget() {
        return target;
    }

    public String getRequestCookie(String string, String defaultValue) {
        String cookie = getRequestCookie(string);
        if (cookie == null) {
            cookie = defaultValue;
        }
        return cookie;
    }

    public String getRequestCookie(String string) {

        String value = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(string)) {
                value = cookie.getValue();
                break;
            }
        }

        return value;
    }

    public void addCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
//        cookie.setPath("");
//        cookie.setComment("");
//        cookie.setDomain("");
//        cookie.setMaxAge(0);
//        cookie.setSecure(false);
//        cookie.setValue(value);
//        cookie.setVersion(0);            
        response.addCookie(cookie);
    }

}
