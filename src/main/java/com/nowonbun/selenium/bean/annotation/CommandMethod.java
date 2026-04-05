package com.nowonbun.selenium.bean.annotation;

import com.nowonbun.selenium.bean.NbCommand;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandMethod {
  NbCommand value();
}
