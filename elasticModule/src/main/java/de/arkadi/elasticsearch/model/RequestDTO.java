package de.arkadi.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestDTO {

  String text;
  ArrayList<String> tags;
  Integer request_id;
  Integer answer_partition;

  public RequestDTO(String text, Integer request_id, Integer answer_partition) {

    this.request_id = request_id;
    this.answer_partition = answer_partition;
    this.text = text;
    this.tags = new ArrayList<>();
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

  public ArrayList<String> getTags() {

    return tags;
  }

  public void setTags(ArrayList<String> tags) {

    this.tags = tags;
  }

}
