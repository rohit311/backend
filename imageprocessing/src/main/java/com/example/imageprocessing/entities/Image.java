package com.example.imageprocessing.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long imageID;

    private String name;
    private String type;

    @Lob
    @Column(name = "picByte", length = 5000)
    private byte[] picByte;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "id")
    private User users_id; //mapping with User entity

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }

    public Image setImageName(String name) {
      this.name = name;

      return this;
    }

    public Image setType(String type) {
      this.type = type;

      return this;
    }
}

