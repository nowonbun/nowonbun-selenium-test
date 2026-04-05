package com.nowonbun.selenium.bean;

import org.openqa.selenium.WebElement;

public class NbWebElement {
  /*adapterパータンで動きをすべて制御する。 */
  private WebElement element = null;

  public NbWebElement() {}

  public void setElement(WebElement element) {
    this.element = element;
  }

  public void setValue(String value) {
    if (element == null) throw new IllegalStateException("element is null");
    element.sendKeys(value);
  }

  public void submit() {
    if (element == null) throw new IllegalStateException("element is null");
    element.submit();
  }

  public void click() {
    if (element == null) throw new IllegalStateException("element is null");
    element.click();
  }

  public void click(NbChromeDriver nowonbunChromeDriver) {
    if (element == null) throw new IllegalStateException("element is null");
    nowonbunChromeDriver.executeScript("arguments[0].click();", element);
  }

  public void dbClick(NbChromeDriver nowonbunChromeDriver) {
    if (element == null) throw new IllegalStateException("element is null");
    nowonbunChromeDriver.getActions().doubleClick(element).perform();
  }

  public void select(String value) {
    if (element == null) throw new IllegalStateException("element is null");
    var select = new org.openqa.selenium.support.ui.Select(element);
    select.selectByValue(value);
  }

  public void select(Integer value) {
    if (element == null) throw new IllegalStateException("element is null");
    var select = new org.openqa.selenium.support.ui.Select(element);
    select.selectByIndex(value);
  }

  public void executeScript(String script, Object... args) {
    if (element == null) throw new IllegalStateException("element is null");
    ((org.openqa.selenium.JavascriptExecutor) element).executeScript(script, args);
  }
}
