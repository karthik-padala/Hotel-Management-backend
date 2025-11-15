package com.project.airBnbApp.service;

import com.project.airBnbApp.dto.ProfileUpdateRequestDto;
import com.project.airBnbApp.entity.User;

public interface UserService {
    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);
}
