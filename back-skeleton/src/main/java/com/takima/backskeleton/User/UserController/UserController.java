package com.takima.backskeleton.User.UserController;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.takima.backskeleton.User.UserService.UserService;
import com.takima.backskeleton.User.DTO.UserResponse;
import com.takima.backskeleton.User.DTO.UserCreateRequest;

import com.takima.backskeleton.User.DTO.UserUpdateRequest;
 
import java.util.UUID;
 
@RestController
@RequestMapping("/api/users")
public class UserController {
 
    private final UserService userService;
 
    public UserController(UserService userService) {
        this.userService = userService;
    }
 
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserCreateRequest request) {
        return userService.register(request);
    }
 
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.getById(currentUserId(authentication));
    }
 
    @PutMapping("/me")
    public UserResponse update(Authentication authentication,
                               @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(currentUserId(authentication), request);
    }
 
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication) {
        userService.delete(currentUserId(authentication));
    }
 
    @GetMapping(value = "/me/export", produces = "application/json")
    public UserResponse export(Authentication authentication) {
        return userService.getById(currentUserId(authentication));
    }
 
    private static UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}

