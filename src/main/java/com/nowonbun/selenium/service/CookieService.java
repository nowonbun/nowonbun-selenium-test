package com.nowonbun.selenium.service;

import org.openqa.selenium.Cookie;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Set;

@Component
public class CookieService extends HashMap<String, Set<Cookie>> {

  public void setCookies(String domain, Set<Cookie> cookies) {
    this.put(domain, cookies);
  }

  public Set<Cookie> getCookies(String domain) {
    return this.get(domain);
  }
}
