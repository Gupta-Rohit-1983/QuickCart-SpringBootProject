package com.quickcart.main.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.quickcart.main.model.User;
import com.quickcart.main.repository.UserRepository;
import com.quickcart.main.service.UserService;
import com.quickcart.main.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("username");
        User user = userRepository.findByEmail(email);

        if (user != null) {
            if (user.getIsEnable()) {
                if (user.getAccountNonLocked()) {
                    if (user.getFailedAttempt() < AppConstant.ATTEMPT_TIME) {
                        userService.increaseFailedAttempt(user);
                    } else {
                        userService.lockUserAccount(user);
                        exception = new LockedException("Your Account is Locked || Failed Attempt 3");
                    }
                } else {
                    if (userService.unlockAccount(user)) {
                        exception = new LockedException("Your Account is Unlocked || Please try Again to login ");
                    } else {
                        exception = new LockedException("Your Account is Locked || Please try after some time");
                    }
                }
            } else {
                exception = new LockedException("Your Account is Inactive");
            }
        } else {
            exception = new LockedException("Email and Password is Invalid");

        }

        super.setDefaultFailureUrl("/signin?error");
        super.onAuthenticationFailure(request, response, exception);
    }

}
