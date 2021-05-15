package com.example.cowin.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import javax.validation.constraints.NotNull;

@Entity // This tells Hibernate to make a table out of this class
@Table(name = "covin_data")
public class CovinData {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String pincode;

  private String date;

  @NotNull
  @Column(length=65555)
  private String response;

  private LocalDateTime createdAt = LocalDateTime.now();

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getPincode() {
    return this.pincode;
  }

  public void setPincode(String pincode) {
    this.pincode = pincode;
  }

  public String getDate() {
    return this.date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
}

}
