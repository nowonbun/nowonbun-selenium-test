package com.nowonbun.selenium.service;

import com.nowonbun.selenium.bean.NbCommand;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class WebService extends AbstractService {

  public WebService(ThreadPoolTaskExecutor seleniumExecutor, CookieService cookieService) {
    super(seleniumExecutor, cookieService);
  }

  /** TODO: 実装用のテンプレート */
  public void test() {
    super.execute(
        "https://nowonbun-alpha.linecorp.com/",
        driver -> {
          super.run(driver, NbCommand.INPUT_AND_SUBMIT, "#username", "soonyub.hwang");
          super.run(driver, NbCommand.WAIT, "10");
          super.run(driver, NbCommand.SCROLL_BOTTOM);
          super.run(driver, NbCommand.WAIT, "10");
          super.run(driver, NbCommand.SCREENSHOT, "C:/work/login.png");
        });
  }
}
