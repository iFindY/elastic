package de.arkadi.elasticsearch.model;

public class SearchDTO {

  private String request_id;
  private String answer_partition;
  private String request;

  public SearchDTO(String request) {

    this.request = request;
  }

  public String getRequest() {

    return request;
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

  public void setRequest(String request) {

    this.request = request;
  }
}
