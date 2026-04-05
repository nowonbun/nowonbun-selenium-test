package com.nowonbun.selenium.service;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class CommandService extends AbstractService {

  public CommandService(ThreadPoolTaskExecutor seleniumExecutor, CookieService cookieService) {
    super(seleniumExecutor, cookieService);
  }
}
