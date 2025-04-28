package com.quickcart.main.controller;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.quickcart.main.model.Category;
import com.quickcart.main.model.Product;
import com.quickcart.main.model.User;
import com.quickcart.main.service.CartService;
import com.quickcart.main.service.CategoryService;
import com.quickcart.main.service.ProductService;
import com.quickcart.main.service.UserService;
import com.quickcart.main.util.CommonUtil;

import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CartService cartService;

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

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("categories", categoryService.getAllActiveCategory());
        List<Product> allActiveProducts = productService.getAllActiveProducts("").stream()
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                .limit(8).toList();
        model.addAttribute("products", allActiveProducts);
        return "index";
    }

    @GetMapping("/signin")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/products")
    public String product(Model model, @RequestParam(value = "category", defaultValue = "") String category,
            @RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(defaultValue = "") String ch) {
        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        model.addAttribute("categories", allActiveCategory);
        model.addAttribute("paramValue", category);

        // List<Product> allActiveProducts =
        // productService.getAllActiveProducts(category);
        // model.addAttribute("products", allActiveProducts);

        Page<Product> page = null;

        if (StringUtils.isEmpty(ch)) {
            page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
        } else {
            page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
        }

        List<Product> products = page.getContent();
        model.addAttribute("productSize", products.size());
        model.addAttribute("products", products);
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElement", page.getTotalElements());
        model.addAttribute("totalPage", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        return "product";
    }

    @GetMapping("/product/{id}")
    public String viewProduct(@PathVariable int id, Model model) {
        Product productById = productService.getProductById(id);
        model.addAttribute("product", productById);
        return "view_product";
    }

    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute User user, HttpSession session) {

        Boolean exitsEmail = userService.exitsEmail(user.getEmail());

        if (exitsEmail) {
            session.setAttribute("errorMsg", "Email ID is already Exits");
        } else {
            User saveUser = userService.saveUser(user);

            if (!ObjectUtils.isEmpty(saveUser)) {
                session.setAttribute("successMsg", "Registration is Successfull");
            } else {
                session.setAttribute("errorMsg", "Some Error Occur");
            }
        }

        return "redirect:/register";
    }

    @GetMapping("/forgot-password")
    public String loadForgotPassword() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request)
            throws UnsupportedEncodingException, MessagingException {
        User userByEmail = userService.getUserByEmail(email);

        if (ObjectUtils.isEmpty(userByEmail)) {
            session.setAttribute("errorMsg", "Invalid Email Id");
        } else {

            String resetToken = UUID.randomUUID().toString();

            userService.updateUserResetToken(email, resetToken);

            String url = commonUtil.generatedURL(request) + "/reset-password?token=" + resetToken;

            Boolean sendEmail = commonUtil.sendEmail(url, email);

            if (sendEmail) {
                session.setAttribute("successMsg", "Please Check your mail reset link is send");
            } else {
                session.setAttribute("errorMsg", "Mail is not sent");
            }
        }
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String loadResetPassword(@RequestParam String token, HttpSession session, Model model) {
        User userByToken = userService.getUserByToken(token);

        if (ObjectUtils.isEmpty(userByToken)) {
            model.addAttribute("msg", "Your link is invaid or expired");
            return "massage";
        }
        model.addAttribute("token", token);
        return "resetPassword";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, HttpSession session, Model model,
            @RequestParam String password) {
        User userByToken = userService.getUserByToken(token);

        if (ObjectUtils.isEmpty(userByToken)) {
            model.addAttribute("msg", "Your link is invaid or expired");
            return "massage";
        } else {
            userByToken.setPassword(passwordEncoder.encode(password));
            userByToken.setResetToken(null);
            userService.updateUser(userByToken);
            model.addAttribute("msg", "Your Password is reset successfull");
            return "massage";
        }
    }

    @GetMapping("search")
    public String searchProduct(@RequestParam String ch, Model model) {
        List<Product> searchProduct = productService.searchProduct(ch);
        model.addAttribute("products", searchProduct);
        return "product";
    }

}
