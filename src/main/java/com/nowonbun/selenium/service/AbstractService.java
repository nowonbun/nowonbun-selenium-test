package com.nowonbun.selenium.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowonbun.selenium.bean.*;
import com.nowonbun.selenium.bean.annotation.CommandMethod;
import jakarta.annotation.PostConstruct;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractService {
  @Value("${spring.profiles.active}")
  private String env;

  @Value("${app.chrome-size}")
  private String chromeSize;

  private ChromeOptions options;
  private final ThreadPoolTaskExecutor executor;
  public static final ObjectMapper objectMapper = new ObjectMapper();
  private final CookieService cookieService;
  private final List<Method> methods =
      Stream.of(AbstractService.class.getDeclaredMethods())
          .filter(m -> m.isAnnotationPresent(CommandMethod.class))
          .toList();

  public AbstractService(ThreadPoolTaskExecutor executor, CookieService cookieService) {
    this.executor = executor;
    this.cookieService = cookieService;
  }

  @PostConstruct
  private void initialize() {
    options = new ChromeOptions();
    options.setBrowserVersion("stable");
    if (!"local".equals(env)) {
      // ブラウザを隠す
      options.addArguments("--headless=new");
      // イメージレンダリングを消す。
      // options.addArguments("--blink-settings=imagesEnabled=false");
    }
    // 拡張機能OFF
    options.addArguments("--disable-extensions");
    options.addArguments("--window-size=" + chromeSize);
  }

  protected void execute(String url, RunnableExecutor runnable) {
    executor.submit(
        () -> {
          try (var driver = new NbChromeDriver(options)) {
            driver.navigate(url);
            runnable.run(driver);
          } catch (Throwable e) {
            e.printStackTrace();
          }
        });
  }

  public NbCommandLine getCommand(int idx) {
    return null;
  }

  public NbCommandLine decoding(String script) throws JsonProcessingException {
    var ret = objectMapper.readValue(script, NbCommandLine.class);
    isValid(ret);
    return ret;
  }

  /**
   * コマンドの妥当性チェック 必ず、STARTで始まり、CLOSEで終わること。
   *
   * @param cmd
   */
  private void isValid(NbCommandLine cmd) {
    if (cmd == null || cmd.isEmpty()) {
      throw new IllegalArgumentException("Command is empty");
    }
    if (cmd.getFirst().getCommand() != NbCommand.START) {
      throw new IllegalArgumentException("First command must be START");
    }
    if (cmd.getLast().getCommand() != NbCommand.CLOSE) {
      throw new IllegalArgumentException("Last command must be CLOSE");
    }
  }

  public void execute(NbCommandLine cmd) {
    var first = cmd.pop();
    if (first.getCommand() != NbCommand.START) {
      throw new IllegalArgumentException("First command must be URL");
    }
    this.execute(
        first.getTarget(),
        (driver -> {
          CommandNode c;
          while ((c = cmd.poll()) != null) {
            if (NbCommand.CLOSE == c.getCommand()) {
              return;
            }
            run(driver, c.getCommand(), c.getTarget(), c.getValue());
          }
        }));
  }

  protected void run(NbChromeDriver driver, NbCommand cmd) {
    run(driver, cmd, null, null);
  }

  protected void run(NbChromeDriver driver, NbCommand cmd, String target) {
    run(driver, cmd, target, null);
  }

  protected void run(NbChromeDriver driver, NbCommand cmd, String target, String value) {
    var op =
        methods.stream().filter(x -> x.getAnnotation(CommandMethod.class).value() == cmd).findAny();
    if (op.isPresent()) {
      var m = op.get();
      try {
        if (m.getParameterCount() == 1) {
          m.invoke(this, driver);
        } else if (m.getParameterCount() == 2) {
          m.invoke(this, driver, target);
        } else if (m.getParameterCount() == 3) {
          m.invoke(this, driver, target, value);
        } else {
          throw new IllegalArgumentException("Invalid method parameter count: " + m);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new IllegalArgumentException("Unknown command: " + cmd);
    }
  }

  @CommandMethod(NbCommand.URL)
  private void url(NbChromeDriver driver, String target) {
    driver.navigate(target);
    driver.waitReadyState();
  }

  @CommandMethod(NbCommand.CLICK)
  private void click(NbChromeDriver driver, String target) {
    var e = driver.findElementByCssSelector(target);
    e.click();
  }

  @CommandMethod(NbCommand.CLICK2)
  private void click2(NbChromeDriver driver, String target) {
    var e = driver.findElementByCssSelector(target);
    e.click(driver);
  }

  @CommandMethod(NbCommand.DBL_CLICK)
  private void dblClick(NbChromeDriver driver, String target) {
    var e = driver.findElementByCssSelector(target);
    e.dbClick(driver);
  }

  @CommandMethod(NbCommand.INPUT)
  private void input(NbChromeDriver driver, String target, String value) {
    var e = driver.findElementByCssSelector(target);
    e.setValue(value);
  }

  @CommandMethod(NbCommand.SUBMIT)
  private void submit(NbChromeDriver driver, String target) {
    var e = driver.findElementByCssSelector(target);
    e.submit();
  }

  @CommandMethod(NbCommand.INPUT_AND_SUBMIT)
  private void inputAndSubmit(NbChromeDriver driver, String target, String value) {
    var e = driver.findElementByCssSelector(target);
    e.setValue(value);
    e.submit();
  }

  @CommandMethod(NbCommand.SELECT)
  private void select(NbChromeDriver driver, String target, String value) {
    var e = driver.findElementByCssSelector(target);
    e.select(value);
  }

  @CommandMethod(NbCommand.SELECT_IDX)
  private void selectIdx(NbChromeDriver driver, String target, String value) {
    var e = driver.findElementByCssSelector(target);
    e.select(Integer.parseInt(value));
  }

  @CommandMethod(NbCommand.SWITCH_FRAME)
  private void switchFrame(NbChromeDriver driver, String target) {
    driver.switchTo(target);
  }

  @CommandMethod(NbCommand.SWITCH_PARENT_FRAME)
  private void switchParentFrame(NbChromeDriver driver) {
    driver.switchToParent();
  }

  @CommandMethod(NbCommand.SWITCH_DEFAULT_FRAME)
  private void switchDefaultFrame(NbChromeDriver driver) {
    driver.switchToDefault();
  }

  @CommandMethod(NbCommand.SWITCH_WINDOW)
  private void switchWindow(NbChromeDriver driver) {
    driver.switchToWindow();
  }

  @CommandMethod(NbCommand.SWITCH_PARENT_WINDOW)
  private void switchParentWindow(NbChromeDriver driver) {
    driver.closeOthersAndSwitchToParent();
  }

  @CommandMethod(NbCommand.ALERT_ACCEPT)
  private void alertAccept(NbChromeDriver driver) {
    driver.acceptAlert();
  }

  @CommandMethod(NbCommand.ALERT_DISMISS)
  private void alertDismiss(NbChromeDriver driver) {
    driver.dismissAlert();
  }

  @CommandMethod(NbCommand.SET_COOKIE)
  private void setCookie(NbChromeDriver driver, String target) {
    cookieService.setCookies(target, driver.getCookie());
  }

  @CommandMethod(NbCommand.LOAD_COOKIE)
  private void loadCookie(NbChromeDriver driver, String target) {
    driver.setCookie(cookieService.getCookies(target));
  }

  @CommandMethod(NbCommand.WAIT)
  private void wait(NbChromeDriver driver, String target) {
    driver.wait(Integer.parseInt(target));
  }

  @CommandMethod(NbCommand.SCREENSHOT)
  private void screenshot(NbChromeDriver driver, String target) {
    driver.captureScreenshot(target);
  }

  @CommandMethod(NbCommand.SCROLL_TO)
  private void scrollTo(NbChromeDriver driver, String target) {
    driver.scrollTo(Integer.parseInt(target));
  }

  @CommandMethod(NbCommand.SCROLL_TOP)
  private void scrollTop(NbChromeDriver driver) {
    driver.scrollToTop();
  }

  @CommandMethod(NbCommand.SCROLL_BOTTOM)
  private void scrollBottom(NbChromeDriver driver) {
    driver.scrollToBottom();
  }
}


