package com.assignment.userservice.controller;

import com.assignment.userservice.dto.CreateUserRequest;
import com.assignment.userservice.entity.UserEntity;
import com.assignment.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserEntity> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserEntity createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}