package com.quickcart.main.service;

import java.util.List;

import com.quickcart.main.model.User;

public interface UserService {

    public User saveUser(User user);

    public User getUserByEmail(String email);

    public List<User> getUser(String role);

    public Boolean updateStatus(Integer id, Boolean status);

    public void increaseFailedAttempt(User user);

    public void lockUserAccount(User user);

    public Boolean unlockAccount(User user);

    public void resetAttempt(int id);

    public void updateUserResetToken(String email, String resetToken);

    public User getUserByToken(String token);

    public User updateUser(User user);

    public User updatUserProfile(User user);

    // public User saveAdmin(User user);

    public Boolean exitsEmail(String email);
}
