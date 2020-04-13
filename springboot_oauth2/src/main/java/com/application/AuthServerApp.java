package com.application;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScans({@ComponentScan(basePackages = {"com.zh.spring","com.application.core.config"})})
@MapperScan(basePackages = {"com.zh.spring"} )
@EnableDiscoveryClient
public class AuthServerApp {
  public static void main(String[] args) {
    SpringApplication.run(AuthServerApp.class, args);
  }

}
