package com.github.supreme94.wechat.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class HttpClient {

  public static ResponseEntity<String> getRequest(HttpHeaders headers, String url) {
    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
    return response;
  }

  public static HttpHeaders getHeaders(String key, String value) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(key, value);
    return headers;
  }

  public static ResponseEntity<String> postJsonRequest(String jsonString, String requestUrl, Object... uriParams) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    try {
      headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
      HttpEntity<String> httpEntity = new HttpEntity<String>(jsonString, headers);
      return restTemplate.postForEntity(requestUrl, httpEntity, String.class,uriParams);
    }
    catch (HttpServerErrorException e) {
      System.out.println("[ERROR] " + requestUrl + "\n[JSON] " + jsonString);
    }
    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
  }

  public static ResponseEntity<Object> sendRequest(MediaType mediaType, MultiValueMap<String, String> paramMap,
      String requestMethod, String requestUrl, Object... uriParams) {
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<Object> response;
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(paramMap,
        headers);
    switch (requestMethod) {
    case "GET":
      response = restTemplate.getForEntity(requestUrl, Object.class, uriParams);
      break;
    case "POST":
      response = restTemplate.postForEntity(requestUrl, httpEntity, Object.class, uriParams);
      break;
    default:
      response = restTemplate.getForEntity(requestUrl, Object.class);
      break;
    }
    return response;
  }

}
