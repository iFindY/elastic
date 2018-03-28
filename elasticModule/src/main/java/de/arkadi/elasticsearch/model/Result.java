package de.arkadi.elasticsearch.model;

import java.io.Serializable;
import java.util.List;

public class Result implements Serializable {

  private List resultList;

  public Result(List result) {

    resultList = result;
  }

  public List getResult() {

    return resultList;
  }
}
