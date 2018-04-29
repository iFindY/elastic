package de.arkadi.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.Map;

public class SaveDTO {

  private String id;
  private String text;
  private ArrayList<String> tags;

  public String getId() {

    return id;
  }

  @JsonSetter("uhh_id")
  public void setId(String id) {

    this.id = id;
  }

  public ArrayList<String> getTags() {

    return tags;
  }

  public void setTags(ArrayList<String> tags) {

    this.tags = tags;
  }

  public String getText() {

    return text;
  }

  public void setText(String text) {

    this.text = text;
  }

  @SuppressWarnings("unchecked")
  @JsonProperty("tweet")
  private void setText(Map<String, Object> tweet) {

    this.text = (String) tweet.get("text");
    this.tags = (ArrayList<String>) ((Map<String, Object>) tweet.get("entities")).get("hashtags");
  }

  @Override
  public String toString() {

    return "person: " + id + " message: " + text;
  }
}
