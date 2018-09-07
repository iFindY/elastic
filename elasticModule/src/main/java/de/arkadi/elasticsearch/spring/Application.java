package de.arkadi.elasticsearch.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages="de.arkadi.elasticsearch")
public class Application extends SpringBootServletInitializer {

  public static void main(String[] args) {

    SpringApplication.run(AppConfig.class, args);
  }

}
