package de.arkadi.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SaveDTO {

  private String id;
  private String text;
  private Long timeStamp;
  private List<String> tags;
  private List<String> users;

  public String getId() {

    return id;
  }

  @JsonSetter("uhh_id")
  public void setId(String id) {

    this.id = id;
  }

  public List<String> getTags() {

    return tags;
  }

  public void setTags(ArrayList<String> tags) {

    this.tags = tags;
  }

  public String getText() {

    return text;
  }

  public void setTimeStamp(Long timeStamp) {

    this.timeStamp = timeStamp;
  }

  public void setTags(List<String> tags) {

    this.tags = tags;
  }

  public void setUsers(List<String> users) {

    this.users = users;
  }

  public void setText(String text) {

    this.text = text;
  }

  public List<String> getUsers() {

    return users;
  }

  @SuppressWarnings("unchecked")
  @JsonProperty("tweet")
  private void setText(Map<String, Object> tweet) {

    this.text = (String) tweet.get("text");
    this.timeStamp = (Long) tweet.get("timestamp_ms");
    Stream<String> temp = Arrays.stream(text.split("\\s+"));
    this.tags = temp
      .filter(x -> x.startsWith("#")).
        map(x -> x.substring(1))
      .collect(Collectors.toList());
    this.users = temp
      .filter(x -> x.startsWith("@")).
        map(x -> x.substring(1))
      .collect(Collectors.toList());
  }

  public Long getTimeStamp() {

    return timeStamp;
  }

  @Override
  public String toString() {

    return "person: "
           + id
           + ", message: "
           + text;
  }

}
