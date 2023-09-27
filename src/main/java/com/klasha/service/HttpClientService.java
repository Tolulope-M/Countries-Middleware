package com.klasha.service;

import java.io.Serializable;

public interface HttpClientService {

   <T,D> T initiatePostRequest(Class<T> clazz, D request, String url);
}
