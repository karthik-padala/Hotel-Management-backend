package com.project.airBnbApp.dto;

import com.project.airBnbApp.entity.User;
import com.project.airBnbApp.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class GuestDto {


    private Long id;
    private String name;
    private Gender gender;
    private Integer dateOfBirth;
}
