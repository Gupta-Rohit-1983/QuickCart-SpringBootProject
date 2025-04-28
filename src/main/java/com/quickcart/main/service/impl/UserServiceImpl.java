package com.quickcart.main.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.quickcart.main.model.User;
import com.quickcart.main.repository.UserRepository;
import com.quickcart.main.service.UserService;
import com.quickcart.main.util.AppConstant;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User saveUser(User user) {
        user.setRole("ROLE_USER");
        user.setIsEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        user.setLockTime(null);

        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        User saveUser = userRepository.save(user);
        return saveUser;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getUser(String role) {
        return userRepository.findByRole(role);
    }

    @Override
    public Boolean updateStatus(Integer id, Boolean status) {
        Optional<User> byId = userRepository.findById(id);

        if (byId.isPresent()) {
            User user = byId.get();
            user.setIsEnable(status);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void increaseFailedAttempt(User user) {
        int attempt = user.getFailedAttempt() + 1;
        user.setFailedAttempt(attempt);
        userRepository.save(user);
    }

    @Override
    public void lockUserAccount(User user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public Boolean unlockAccount(User user) {
        long lockTime = user.getLockTime().getTime();
        long unlockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;

        long currentTime = System.currentTimeMillis();

        if (unlockTime < currentTime) {
            user.setAccountNonLocked(true);
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userRepository.save(user);

            return true;
        }
        return false;
    }

    @Override
    public void resetAttempt(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resetAttempt'");
    }

    @Override
    public void updateUserResetToken(String email, String resetToken) {
        User byEmail = userRepository.findByEmail(email);
        byEmail.setResetToken(resetToken);
        userRepository.save(byEmail);
    }

    @Override
    public User getUserByToken(String token) {
        return userRepository.findByResetToken(token);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updatUserProfile(User user) {

        User dbUser = userRepository.findById(user.getId()).get();

        if (!ObjectUtils.isEmpty(dbUser)) {
            dbUser.setName(user.getName());
            dbUser.setMobileNumber(user.getMobileNumber());
            dbUser.setArea(user.getArea());
            dbUser.setCity(user.getCity());
            dbUser.setState(user.getState());
            dbUser.setPincode(user.getPincode());

            dbUser = userRepository.save(dbUser);
        }

        return dbUser;
    }

    @Override
    public Boolean exitsEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // @Override
    // public User saveAdmin(User user) {

    // user.setRole("ROLE_ADMIN");
    // user.setIsEnable(true);
    // user.setAccountNonLocked(true);
    // user.setFailedAttempt(0);
    // user.setLockTime(null);

    // String encodePassword = passwordEncoder.encode(user.getPassword());
    // user.setPassword(encodePassword);
    // User saveUser = userRepository.save(user);
    // return saveUser;
    // }

    

}
