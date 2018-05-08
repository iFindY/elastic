package de.arkadi.elasticsearch.model;

public class DeleteDTO {

  private String id;

  public DeleteDTO() {

  }

  public DeleteDTO(String id) {

    this.id = id;
  }

  public String getId() {

    return id;
  }

  public void setId(String id) {

    this.id = id;
  }

  @Override
  public String toString(){
    return "delete item: " + id;

  }
}
