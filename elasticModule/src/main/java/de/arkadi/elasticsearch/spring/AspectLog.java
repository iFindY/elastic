package de.arkadi.elasticsearch.spring;

import de.arkadi.elasticsearch.elasticsearch.repository.MessageRepository;
import de.arkadi.elasticsearch.model.ResultDTO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
public class AspectLog {

  private static final Logger log = LoggerFactory.getLogger(MessageRepository.class);

  @Pointcut("execution (de.arkadi.elasticsearch.model.ResultDTO de.arkadi.elasticsearch.elasticsearch.repository.*.*(..))")
  private void repoget() {

  }

  @Pointcut("execution (org.elasticsearch.rest.RestStatus de.arkadi.elasticsearch.elasticsearch.repository.*.*(..))")
  private void reposet() {

  }

/*
  @Before("repo()")
  public void logQuery(JoinPoint joinPoint) {

    String signature = joinPoint.getSignature().getName();
    List<String> query = Arrays.stream(joinPoint.getArgs())
      .map(Object::toString)
      .collect(Collectors.toList());
    log.info("method '{}'  received  :'{}'", signature, query.toString());

  }
*/

  @AfterReturning(pointcut = "repoget()", returning = "results")
  public void logGetRepository(JoinPoint joinPoint, ResultDTO results) {

    String signature = joinPoint.getSignature().getName();
    List<String> query = Arrays.stream(joinPoint.getArgs())
      .map(Object::toString)
      .collect(Collectors.toList());
    log.info(" method: '{}' received: '{}' and returned: '{}'",
             signature,
             query.toString(),
             results.getResultList());
  }

  @AfterReturning(pointcut = "reposet()", returning = "result")
  public void logSaveRepository(JoinPoint joinPoint, RestStatus result) {

    String signature = joinPoint.getSignature().getName();
    List<String> query = Arrays.stream(joinPoint.getArgs())
      .map(Object::toString)
      .collect(Collectors.toList());
    log.info(" method: '{}'  received: '{}' and returned: '{}'",
             signature,
             query.toString(),
             result.toString());
  }

}
