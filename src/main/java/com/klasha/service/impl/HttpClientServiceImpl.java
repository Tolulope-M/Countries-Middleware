package com.klasha.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klasha.exception.ServerSideException;
import com.klasha.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import static com.klasha.constants.AppConstants.APPLICATION_JSON;

@RequiredArgsConstructor
@Service
public class HttpClientServiceImpl implements HttpClientService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Override
    public <T,D> T initiatePostRequest(Class<T> clazz, D request, String url) {
        try {
            HttpPost httpPost = new HttpPost(url);
            String requestBody = objectMapper.writeValueAsString(request);
            StringEntity entity = new StringEntity(requestBody);
            httpPost.setEntity(entity);
            entity.setContentType(APPLICATION_JSON);

            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                throw new ServerSideException("HTTP request failed with code: " + statusCode);
            }

            return objectMapper.readValue(EntityUtils.toString(response.getEntity()), clazz);
        }
        catch (Exception e){
            throw new ServerSideException(e.getMessage());
        }
    }
}
