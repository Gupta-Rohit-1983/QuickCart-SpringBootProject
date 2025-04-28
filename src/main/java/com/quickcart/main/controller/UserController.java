package com.quickcart.main.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quickcart.main.model.Cart;
import com.quickcart.main.model.Category;
import com.quickcart.main.model.OrderRequest;
import com.quickcart.main.model.ProductOrder;
import com.quickcart.main.model.User;
import com.quickcart.main.service.CartService;
import com.quickcart.main.service.CategoryService;
import com.quickcart.main.service.OrderService;
import com.quickcart.main.service.UserService;
import com.quickcart.main.util.CommonUtil;
import com.quickcart.main.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home() {
        return "user/home";
    }

    @ModelAttribute
    public void getUserDetails(Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            User user = userService.getUserByEmail(email);
            model.addAttribute("user", user);

            Integer cartCount = cartService.getCartCount(user.getId());
            model.addAttribute("cartCount", cartCount);
        }
        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        model.addAttribute("categories", allActiveCategory);
    }

    @GetMapping("/addCart")
    public String addToCart(@RequestParam int pid, @RequestParam int uid, HttpSession session) {
        Cart saveCart = cartService.saveCart(pid, uid);
        if (ObjectUtils.isEmpty(saveCart)) {
            session.setAttribute("errorMsg", "Product is not added");
        } else {
            session.setAttribute("successMsg", "Product Added Successfully");
        }
        return "redirect:/product/" + pid;
    }

    @GetMapping("/cart")
    public String loadCart(Principal p, Model model) {
        User user = getLoggedUser(p);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        model.addAttribute("carts", carts);
        if (carts.size() > 0) {
            Double totalOrderAmount = carts.get(carts.size() - 1).getTotalOrderAmount();
            model.addAttribute("totalOrderPrice", totalOrderAmount);
        }
        return "user/cart";
    }

    @GetMapping("/updateCart")
    public String updatedCart(String sy, @RequestParam Integer cid) {
        cartService.updateQuantity(sy, cid);
        return "redirect:/user/cart";
    }

    private User getLoggedUser(Principal p) {
        String email = p.getName();
        User user = userService.getUserByEmail(email);
        return user;
    }

    @GetMapping("/order")
    public String orderPage(Principal principal, Model model) {
        User user = getLoggedUser(principal);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        model.addAttribute("carts", carts);
        if (carts.size() > 0) {
            Double totalOrderAmount = carts.get(carts.size() - 1).getTotalOrderAmount();
            model.addAttribute("totalOrderPrice", totalOrderAmount);
        }
        return "user/orders";
    }

    @GetMapping("/success")
    public String success() {
        return "user/success";
    }

    @PostMapping("save-order")
    public String saveOrder(@ModelAttribute OrderRequest request, Principal principal, Model model) {

        User user = getLoggedUser(principal);
        orderService.saveOrder(user.getId(), request);
        // model.addAttribute(null, user);
        return "redirect:/user/success";
    }

    @GetMapping("/user-order")
    public String myOrder(Model model, Principal principal) {
        User user = getLoggedUser(principal);
        List<ProductOrder> orders = orderService.getOrdersByUser(user.getId());
        model.addAttribute("orders", orders);
        return "user/my_order";
    }

    @GetMapping("/update-status")
    public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

        OrderStatus[] values = OrderStatus.values();
        String status = null;
        for (OrderStatus orderStatus : values) {
            if (orderStatus.getId() == st) {
                status = orderStatus.getStatus();
            }
        }

        ProductOrder updateOrder = orderService.updateOrderStatus(id, status);

        try {
            commonUtil.productOrderEmail(updateOrder, status);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!ObjectUtils.isEmpty(updateOrder)) {
            session.setAttribute("successMsg", "Status Updated Successfully");
        } else {
            session.setAttribute("errorMsg", "Some Error Occure");
        }

        return "redirect:/user/user-order";
    }

    @GetMapping("/profile")
    public String profile() {
        return "user/profile";
    }

    @PostMapping("/updateProfile")
    public String updateProfile(@ModelAttribute User user, HttpSession session) {

        User updatUserProfile = userService.updatUserProfile(user);

        if (ObjectUtils.isEmpty(updatUserProfile)) {
            session.setAttribute("errorMsg", "Profile Not Updated");
        } else {
            session.setAttribute("successMsg", "Profile Updated Successfully");
        }

        return "redirect:/user/profile";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword,
            Principal principal, HttpSession session) {

        User loggedUser = getLoggedUser(principal);

        boolean matches = passwordEncoder.matches(currentPassword, loggedUser.getPassword());

        if (matches) {
            String encodedPassword = passwordEncoder.encode(newPassword);
            loggedUser.setPassword(encodedPassword);
            User updateUser = userService.updateUser(loggedUser);

            if (ObjectUtils.isEmpty(updateUser)) {
                session.setAttribute("errorMsg", "Password Not Updated !! Some Error Occur");
            } else {
                session.setAttribute("successMsg", "Password Updated Succesfully");
            }
        } else {
            session.setAttribute("errorMsg", "Current password is incorrect");
        }

        return "redirect:/user/profile";
    }
}
