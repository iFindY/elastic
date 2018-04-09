package de.arkadi.elasticsearch.model;

//TODO insert project specific data
public class DeleteDTO {

  private String id;

  public DeleteDTO(String id) {

    this.id = id;
  }

  public String getId() {

    return id;
  }

  public void setId(String id) {

    this.id = id;
  }
}
