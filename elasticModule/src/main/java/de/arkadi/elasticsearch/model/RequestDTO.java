package de.arkadi.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestDTO {

  String text;
  Integer request_id;
  Integer answer_partition;

  public RequestDTO(String text, Integer request_id, Integer answer_partition) {

    this.request_id = request_id;
    this.answer_partition = answer_partition;
    this.text = text;
  }

  public RequestDTO() {

  }

  public String getText() {

    return text;
  }

  public void setText(String text) {

    this.text = text;
  }

  public Integer getRequest_id() {

    return request_id;
  }

  public void setRequest_id(Integer request_id) {

    this.request_id = request_id;
  }

  public Integer getAnswer_partition() {

    return answer_partition;
  }

  public void setAnswer_partition(Integer answer_partition) {

    this.answer_partition = answer_partition;
  }
}
