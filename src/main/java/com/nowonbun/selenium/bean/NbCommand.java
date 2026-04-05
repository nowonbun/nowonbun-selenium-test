package com.nowonbun.selenium.bean;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum NbCommand {
  START,
  CLOSE,
  URL,
  CLICK,
  CLICK2,
  DBL_CLICK,
  INPUT,
  SUBMIT,
  INPUT_AND_SUBMIT,
  SELECT,
  SELECT_IDX,
  SWITCH_FRAME,
  SWITCH_PARENT_FRAME,
  SWITCH_DEFAULT_FRAME,
  SWITCH_WINDOW,
  SWITCH_PARENT_WINDOW,
  ALERT_ACCEPT,
  ALERT_DISMISS,
  SET_COOKIE,
  LOAD_COOKIE,
  SCROLL_TO,
  SCROLL_TOP,
  SCROLL_BOTTOM,
  WAIT,
  SCREENSHOT;

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static NbCommand from(String s) {
    return NbCommand.valueOf(s.trim().replace(' ', '_').toUpperCase());
  }
}
