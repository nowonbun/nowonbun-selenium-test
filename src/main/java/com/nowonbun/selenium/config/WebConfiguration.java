package com.nowonbun.selenium.config;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

  @Value("${spring.profiles.active}")
  private String env;

  @Bean
  public LocaleResolver localeResolver() {
    CookieLocaleResolver clr = new CookieLocaleResolver();
    clr.setDefaultLocale(Locale.JAPAN);
    clr.setCookieName("language");
    return clr;
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/monitor/check").setViewName("monitor/check");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/static/**")
        .addResourceLocations("classpath:/static/")
        .resourceChain(!"local".equals(env))
        .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
  }

  @Bean
  public FilterRegistrationBean<ResourceUrlEncodingFilter> resourceUrlEncodingFilter() {
    ResourceUrlEncodingFilter filter = new ResourceUrlEncodingFilter();
    FilterRegistrationBean<ResourceUrlEncodingFilter> registrationBean =
        new FilterRegistrationBean<>();
    registrationBean.setFilter(filter);
    registrationBean.addUrlPatterns("/*");
    registrationBean.setOrder(3);
    return registrationBean;
  }

  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
    filter.setIncludeClientInfo(true);
    filter.setIncludeQueryString(true);
    filter.setIncludeHeaders(true);
    filter.setIncludePayload(true);
    filter.setMaxPayloadLength(4096);
    return filter;
  }

  @Override
  public void configureViewResolvers(ViewResolverRegistry registry) {
    registry.jsp().prefix("/").suffix(".jsp").viewNames("jsp/*");
    registry.order(2);
  }
}
