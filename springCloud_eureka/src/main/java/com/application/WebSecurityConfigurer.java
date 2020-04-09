package com.application;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {
  /**
   * 高版本的丢弃了
   * 
   * security: basic: enabled: true
   * 
   * 配置，应该使用以下方式开启 禁用csrf攻击功能 开启所有请求需要验证并且使用http basic进行认证
   *
   * @param http
   * @throws Exception
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
    // 注意：为了可以使用 http://${user}:${password}@${host}:${port}/eureka/ 这种方式登录,所以必须是httpBasic,
    // 如果是form方式,不能使用url格式登录
    http.csrf().disable();
    http.authorizeRequests().anyRequest().authenticated().and().httpBasic();
  }

}
