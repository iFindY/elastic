package de.arkadi.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tweet {

  String text;

  public Tweet() {

  }

  public Tweet(String text) {

    this.text = text;
  }

  public String getText() {

    return text;
  }

  public void setText(String text) {

    this.text = text;
  }
}
