package com.project.airBnbApp.controller;


import com.project.airBnbApp.dto.ProfileUpdateRequestDto;
import com.project.airBnbApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
   private final UserService userService;


   @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto){
       userService.updateProfile(profileUpdateRequestDto);
       return ResponseEntity.noContent().build();
   }








}
