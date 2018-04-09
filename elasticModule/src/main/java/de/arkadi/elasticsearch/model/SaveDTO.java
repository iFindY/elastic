package de.arkadi.elasticsearch.model;

public class SaveDTO {

  private String id;
  private String message;

  public SaveDTO(String id, String message) {

    this.id = id;
    this.message = message;
  }

  public String getId() {

    return id;
  }

  public void setId(String id) {

    this.id = id;
  }

  public String getMessage() {

    return message;
  }

  public void setMessage(String message) {

    this.message = message;
  }

  @Override
  public String toString() {

    return id + "\n " + message + "\n";
  }
}
