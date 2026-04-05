package com.nowonbun.selenium.bean;

import lombok.Getter;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.Actions;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NbChromeDriver implements Closeable {

  /** adapterパータンで動きをすべて制御する。 */
  private final WebDriver adapter;

  @Getter private final NbWebElement element = new NbWebElement();
  @Getter private final Actions actions;

  private final WebDriverWait waitor;
  private String handler = null;

  public NbChromeDriver(ChromeOptions options) {
    this.adapter = new ChromeDriver(options);
    this.waitor = new WebDriverWait(adapter, Duration.ofSeconds(10));
    this.actions = new Actions(adapter);
    this.handler = adapter.getWindowHandle();
  }

  public void wait(int seconds) {
    try {
      Thread.sleep(seconds * 1000L);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public NbWebElement findElementByCssSelector(String cssSelector) {
    element.setElement(waitor.until(d -> d.findElement(By.cssSelector(cssSelector))));
    return element;
  }

  public void waitReadyState() {
    waitor.until(
        d ->
            ((org.openqa.selenium.JavascriptExecutor) d)
                .executeScript("return document.readyState")
                .equals("complete"));
  }

  public void captureScreenshot(String path) {
    // TODO: DBに格納する方法を考える。
    byte[] png =
        ((org.openqa.selenium.TakesScreenshot) this.adapter)
            .getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
    try {
      Files.write(Paths.get(path), png);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void navigate(String url) {
    adapter.get(url);
  }

  @Override
  public void close() throws IOException {
    if (adapter != null) adapter.quit();
  }

  public Object executeScript(String script, Object... args) {
    Object val = ((org.openqa.selenium.JavascriptExecutor) adapter).executeScript(script, args);
    waitReadyState();
    wait(2);
    return val;
  }

  public void switchTo(String cssSelector) {
    WebElement el = waitor.until(d -> d.findElement(By.cssSelector(cssSelector)));
    adapter.switchTo().frame(el);
  }

  // 一つ上のフレームに戻る
  public void switchToParent() {
    adapter.switchTo().parentFrame();
  }

  // 最上位のフレームに戻る
  public void switchToDefault() {
    adapter.switchTo().defaultContent();
  }

  // 必ず新しいポップアップに移動
  public void switchToWindow() {
    for (String h : adapter.getWindowHandles()) {
      if (!h.equals(this.handler)) {
        adapter.switchTo().window(h);
        break;
      }
    }
  }

  // もとのWindowに戻るとポップアップをすべてクローズ
  public void closeOthersAndSwitchToParent() {
    final String main = this.handler;
    List<String> handles = new ArrayList<>(adapter.getWindowHandles());
    for (String h : handles) {
      if (h.equals(main)) continue;
      try {
        adapter.switchTo().window(h);
        adapter.close();
      } catch (UnhandledAlertException e) {
        try {
          adapter.switchTo().alert().dismiss();
        } catch (org.openqa.selenium.NoAlertPresentException ignore) {
        }
        try {
          adapter.close();
        } catch (Exception ignore) {
        }
      } catch (org.openqa.selenium.NoSuchWindowException ignore) {
      }
    }
    new WebDriverWait(adapter, java.time.Duration.ofSeconds(10))
        .until(
            d ->
                d.getWindowHandles().size() == 1
                    || d.getWindowHandles().stream().allMatch(main::equals));

    try {
      adapter.switchTo().window(main);
      adapter.switchTo().defaultContent();
    } catch (org.openqa.selenium.NoSuchWindowException e) {
      throw e;
    }
  }

  public void acceptAlert() {
    try {
      adapter.switchTo().alert().accept();
    } catch (org.openqa.selenium.NoAlertPresentException ignore) {
    }
  }

  public void dismissAlert() {
    try {
      adapter.switchTo().alert().dismiss();
    } catch (org.openqa.selenium.NoAlertPresentException ignore) {
    }
  }

  public void scrollTo(int x) {
    executeScript("window.scrollTo(0,arguments[0]);", x);
  }

  public void scrollTo(int x, int y) {
    executeScript("window.scrollTo(arguments[0], arguments[1]);", x, y);
  }

  public void scrollToTop() {
    executeScript("window.scrollTo(0, 0);");
  }

  public void scrollToBottom() {
    executeScript("window.scrollTo(0, document.body.scrollHeight);");
  }

  public Set<Cookie> getCookie() {
    return adapter.manage().getCookies();
  }

  public void setCookie(Set<Cookie> cookies) {
    if (cookies == null) return;
    for (Cookie c : cookies) {
      adapter.manage().addCookie(c);
    }
  }
}
