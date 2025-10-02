package com.cartoffer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppProps {
  @Value("${segment.baseUrl:http://localhost:1080}")
  private String segmentBaseUrl;

  public String getSegmentBaseUrl() {
    return segmentBaseUrl;
  }
}
