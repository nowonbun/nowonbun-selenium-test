package com.nowonbun.selenium.service;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class LiamHubService extends AbstractService {
  public LiamHubService(ThreadPoolTaskExecutor seleniumExecutor, CookieService cookieService) {
    super(seleniumExecutor, cookieService);
  }
}
