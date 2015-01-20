package com.logginghub.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Resolver {

    public Resolution resolve(String url, HttpServletRequest request, HttpServletResponse response) throws IOException ;
    
}
