package de.arkadi.elasticsearch.model;

import java.util.List;

public class Result {

  private List resultList;

  public Result(List result) {

    resultList = result;
  }

  public List getResult() {

    return resultList;
  }
}
