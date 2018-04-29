package de.arkadi.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tweet {

  private String text;
  private ArrayList<String> tags;

  public Tweet() {

  }

  public Tweet(String text) {

    this.text = text;
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
}
