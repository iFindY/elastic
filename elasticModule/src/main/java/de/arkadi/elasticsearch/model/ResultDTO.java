package de.arkadi.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class ResultDTO implements Serializable {

  @JsonProperty("request_id")
  private Integer requestId;
  @JsonProperty("answer_partition")
  private Integer answerPartition;
  @JsonProperty("tweet_id_list")
  private List resultList;

  public ResultDTO(List resultList, Integer requestId, Integer answerPartition) {

    this.resultList = resultList;
    this.requestId = requestId;
    this.answerPartition = answerPartition;

  }

  public ResultDTO(List resultList) {

    this.resultList = resultList;
  }

  public Integer getRequestId() {

    return requestId;
  }

  public void setRequestId(Integer requestId) {

    this.requestId = requestId;
  }

  public Integer getAnswerPartition() {

    return answerPartition;
  }

  public void setAnswerPartition(Integer answerPartition) {

    this.answerPartition = answerPartition;
  }

  public List getResultList() {

    return resultList;
  }

  public void setResultList(List resultList) {

    this.resultList = resultList;
  }

  @Override
  public String toString() {

    return "Result tweet id list: " + resultList.toString();
  }
}
