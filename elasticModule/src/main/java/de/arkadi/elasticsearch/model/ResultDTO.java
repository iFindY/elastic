package de.arkadi.elasticsearch.model;

import java.io.Serializable;
import java.util.List;

public class ResultDTO implements Serializable {

  private String request_id;
  private String answer_partition;
  private List resultList;

  public ResultDTO(List result) {

    resultList = result;
  }

  public List getResult() {

    return resultList;
  }

  public String getRequest_id() {

    return request_id;
  }

  public void setRequest_id(String request_id) {

    this.request_id = request_id;
  }

  public String getAnswer_partition() {

    return answer_partition;
  }

  public void setAnswer_partition(String answer_partition) {

    this.answer_partition = answer_partition;
  }

  public List getResultList() {

    return resultList;
  }

  public void setResultList(List resultList) {

    this.resultList = resultList;
  }

  @Override
  public String toString() {

    return "\n Result tweet id list :" + resultList.toString() + "\n";
  }
}
