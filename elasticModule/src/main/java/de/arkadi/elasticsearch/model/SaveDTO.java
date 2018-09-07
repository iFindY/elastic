package de.arkadi.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.Gson;
import joptsimple.internal.Strings;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SaveDTO {

  private String id;
  private String text;
  private Long timeStamp;
  private List<String> tags;
  private List<String> users;
  private String userLocation;
  private List<String> completion;
  private Gson gson;

  public SaveDTO(HashMap saveRequest) {

    gson = new Gson();
    this.id = (String) saveRequest.get("uhh_id");
    HashMap<String, Object> tweet = (HashMap<String, Object>) saveRequest.get("tweet");
    this.text = ((String) tweet.get("text")).replaceAll("[\\n\\r\\t\\\\]+", "");
    String location = Optional.ofNullable((String) ((HashMap<String, Object>) tweet.get("user")).get(
      "location")).orElse("null");
    this.userLocation = Strings.isNullOrEmpty(location.trim()) ? "null" : location.replaceAll("[^A-Za-z0-9 ]","");
    this.timeStamp = Long.valueOf(tweet.get("timestamp_ms").toString());
    List<String> tempText = Arrays.asList(text.split("\\s+"));
    List<String> tempLocation = Arrays.asList(this.userLocation.split("\\s+"));
    Function<String, String> normalise = (input)
      -> input.replaceAll("\\W+", "");
    this.tags = tempText.stream()
      .filter(x -> x.startsWith("#")).
        map(normalise)
      .collect(Collectors.toList());
    this.users = tempText.stream()
      .filter(x -> x.startsWith("@")).
        map(normalise)
      .collect(Collectors.toList());
    this.completion = tempLocation.stream()
      .map(normalise).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
  }

  @Nullable
  public String getUserLocation() {

    return userLocation;
  }

  public void setUserLocation(String userLocation) {

    this.userLocation = userLocation;
  }

  public String getId() {

    return id;
  }

  public void setId(String id) {

    this.id = id;
  }

  public String getText() {

    return text;
  }

  public void setText(String text) {

    this.text = text;
  }

  public Long getTimeStamp() {

    return timeStamp;
  }

  public void setTimeStamp(Long timeStamp) {

    this.timeStamp = timeStamp;
  }

  public List<String> getCompletion() {

    return completion;
  }

  @Nullable
  public List<String> getTags() {

    return tags;
  }

  public void setTags(List<String> tags) {

    this.tags = tags;
  }

  @Nullable
  public List<String> getUsers() {

    return users;
  }

  public void setUsers(List<String> users) {

    this.users = users;
  }

  public String createSaveRequest(String save) {

    return save
      .replace("setId", getId())
      .replace("setTags", gson.toJson(getTags()))
      .replace("setUsers", gson.toJson(getUsers()))
      .replace("setInput", gson.toJson(getCompletion()))
      .replace("\"[", "[")
      .replace("]\"", "]")
      .replace("setMessage", getText().replace("\"", "\\\""))
      .replace("setTimeStamp", getTimeStamp().toString())
      .replace("setUserLocation", getUserLocation());
  }

  @Override
  public String toString() {

    return "person: " + id
           + ", time: "
           + timeStamp
           + ", message: "
           + text;
  }

}
