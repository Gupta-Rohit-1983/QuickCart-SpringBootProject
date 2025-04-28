package com.quickcart.main.util;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.quickcart.main.model.ProductOrder;
import com.quickcart.main.model.User;
import com.quickcart.main.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;

    public Boolean sendEmail(String url, String receiverEmail) throws UnsupportedEncodingException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("rg976652@gmail.com", "Quick Cart");
        helper.setTo(receiverEmail);

        String content = "<p>Dear User,</p>" +
                "<p>You have requested for reseting your password of qucik cart account</p>" +
                "<p>Please Click below link to reset your password :</p>" +
                "<p><a href=\"" + url + "\">Change My Password</a><p>";

        helper.setSubject("RESET PASSWORD");
        helper.setText(content, true);
        mailSender.send(message);

        return true;
    }

    public String generatedURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();

        return siteURL.replace(request.getServletPath(), "");
    }

    String msg = null;

    public Boolean productOrderEmail(ProductOrder order, String status) throws Exception {

        msg = "<p>Hii [[name]],</p>" +
            "<p> Thank You for visiting Quick Cart</p>" +
            "<br>" +
            "<p> Your order has been <b>[[orderStatus]]</b></p>" +
            "<p><b>Product Details :</b> </p>" +
            "<p>Order Id : [[orderId]]</p>" +
            "<p>Product Name : [[product]]</p>" +
            "<p>Quantity : [[quantity]]</p>" +
            "<p>Price : [[price]]</p>" +
            "<p>Payment type : [[type]]</p>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("rg976652@gmail.com", "Quick Cart");
        helper.setTo(order.getOrderAddress().getEmail());

        msg = msg.replace("[[name]]", order.getOrderAddress().getFirstName());
        msg = msg.replace("[[orderStatus]]", status);
        msg = msg.replace("[[orderId]]", order.getOrderId());
        msg = msg.replace("[[product]]", order.getProduct().getTitle());
        msg = msg.replace("[[quantity]]", order.getOrderQuantity().toString());
        msg = msg.replace("[[price]]", order.getPrice().toString());
        msg = msg.replace("[[type]]", order.getPaymentType());

        helper.setSubject("ORDER STATUS");
        helper.setText(msg, true);
        mailSender.send(message);

        return true;
    }

    public User getLoggedUser(Principal p) {
        String email = p.getName();
        User user = userService.getUserByEmail(email);
        return user;
    }
}
