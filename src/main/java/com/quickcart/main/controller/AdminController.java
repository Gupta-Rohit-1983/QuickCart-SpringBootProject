package com.quickcart.main.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.quickcart.main.model.Category;
import com.quickcart.main.model.Product;
import com.quickcart.main.model.ProductOrder;
import com.quickcart.main.model.User;
import com.quickcart.main.service.CartService;
import com.quickcart.main.service.CategoryService;
import com.quickcart.main.service.OrderService;
import com.quickcart.main.service.ProductService;
import com.quickcart.main.service.UserService;
import com.quickcart.main.util.CommonUtil;
import com.quickcart.main.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public String index() {
        return "admin/index";
    }

    @GetMapping("/addProduct")
    public String loadAddProduct(Model model) {
        List<Category> allCategories = categoryService.getAllCategories();
        model.addAttribute("categories", allCategories);
        return "admin/add_product";
    }

    @GetMapping("/addCategory")
    public String loadAddCategory(Model model, @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "5") Integer pageSize) {
        // model.addAttribute("categorys", categoryService.getAllCategories());

        Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);
        List<Category> categorys = page.getContent();
        model.addAttribute("categorys", categorys);
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElement", page.getTotalElements());
        model.addAttribute("totalPage", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/add_category";
    }

    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category, HttpSession session,
            @RequestParam("file") MultipartFile file) throws IOException {

        String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
        category.setImageName(imageName);

        if (categoryService.exitsCategory(category.getName())) {
            session.setAttribute("errorMsg", "Category Already Exits");
        } else {
            Category savingCategory = categoryService.saveCategory(category);

            if (ObjectUtils.isEmpty(savingCategory)) {
                session.setAttribute("errorMsg", "Category Not Save | Server Error");
            } else {
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "Category" + File.separator
                        + file.getOriginalFilename());

                // System.out.println(path);

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                session.setAttribute("successMsg", "Category Saved Successfully");
            }
        }

        return "redirect:/admin/addCategory";
    }

    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session) {

        Boolean deleteCategory = categoryService.deleteCategory(id);
        if (deleteCategory) {
            session.setAttribute("successMsg", "Category Delete successfully");
        } else {
            session.setAttribute("errorMsg", "Category Not Delete");
        }

        return "redirect:/admin/addCategory";
    }

    @GetMapping("/editCategory/{id}")
    public String editCategory(@PathVariable int id, HttpSession session, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "admin/editCategory";
    }

    @PostMapping("/updateCategory")
    public String updateCategory(@ModelAttribute Category category, HttpSession session,
            @RequestParam("file") MultipartFile file) throws IOException {

        Category oldCategory = categoryService.getCategoryById(category.getId());
        String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();

        if (!ObjectUtils.isEmpty(category)) {
            oldCategory.setName(category.getName());
            oldCategory.setIsActive(category.getIsActive());
            oldCategory.setImageName(imageName);
        }

        categoryService.saveCategory(oldCategory);

        if (!ObjectUtils.isEmpty(oldCategory)) {
            if (!file.isEmpty()) {
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "Category" + File.separator
                        + file.getOriginalFilename());

                // System.out.println(path);

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }
            session.setAttribute("successMsg", "Category Update Successfully");
        } else {
            session.setAttribute("errorMsg", "Some Error Occur");
        }
        return "redirect:/admin/editCategory/" + category.getId();
    }

    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product, HttpSession session,
            @RequestParam("file") MultipartFile file) throws IOException {

        String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
        product.setImage(imageName);
        product.setDiscount(0);
        product.setDiscountPrice(product.getPrice());

        Product savingProduct = productService.saveProduct(product);

        if (!ObjectUtils.isEmpty(savingProduct)) {

            File saveFile = new ClassPathResource("static/img").getFile();
            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "Products" + File.separator
                    + file.getOriginalFilename());

            // System.out.println(path);

            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            session.setAttribute("successMsg", "Product Added Successfulyy");
        } else {
            session.setAttribute("errorMsg", "Some Error Occur");
        }

        return "redirect:/admin/addProduct";

    }

    @GetMapping("/viewProduct")
    public String loadViewProduct(Model model, @RequestParam(defaultValue = "") String ch,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "5") Integer pageSize) {

        Page<Product> page = null;

        if (ch != null && ch.length() > 0) {
            page = productService.searchProductPagination(pageNo, pageSize, ch);
        } else {
            page = productService.getAllProductsPagination(pageNo, pageSize);
        }
        model.addAttribute("products", page.getContent());
        // List<Product> product = null;

        // if (ch != null && ch.length() > 0) {
        // product = productService.searchProduct(ch);
        // } else {
        // product = productService.getAllProducts();
        // }
        // model.addAttribute("products", product);

        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElement", page.getTotalElements());
        model.addAttribute("totalPage", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/Products";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable int id, HttpSession session) {
        Boolean product = productService.deleteProduct(id);
        if (product) {
            session.setAttribute("successMsg", "Product deleted Successfully");
        } else {
            session.setAttribute("errorMsg", "Some Error Occur");
        }
        return "redirect:/admin/viewProduct";
    }

    @GetMapping("/editProduct/{id}")
    public String editProduct(@PathVariable int id, Model model, HttpSession session) {
        model.addAttribute("product", productService.getProductById(id));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/editProduct";
    }

    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
            HttpSession session, Model model) {

        if (product.getDiscount() < 0 || product.getDiscount() > 100) {
            session.setAttribute("errorMsg", "Invalid Discount Apply");
        } else {

            Product updateProduct = productService.updateProduct(product, file);
            if (!ObjectUtils.isEmpty(updateProduct)) {
                session.setAttribute("successMsg", "Product Updated Successfully");
            } else {
                session.setAttribute("errorMsg", "Some Error Occur");
            }
        }

        return "redirect:/admin/editProduct/" + product.getId();
    }

    @GetMapping("/users")
    public String loadUsers(Model model, @RequestParam Integer type) {
        
        List<User> user = null;
        if (type==1) {
            user=userService.getUser("ROLE_USER");
        }else{
            user=userService.getUser("ROLE_ADMIN");
        }
        model.addAttribute("userType", type);
        model.addAttribute("users", user);
        return "admin/users";
    }

    @GetMapping("/updateSts")
    public String updateUserStatus(@RequestParam Integer id, @RequestParam Boolean status, @RequestParam Integer type, HttpSession session) {
        Boolean updatedStatus = userService.updateStatus(id, status);

        if (updatedStatus) {
            session.setAttribute("successMsg", "User Status Updated Successfully");
        } else {
            session.setAttribute("errorMsg", "Some Error Occur");
        }

        return "redirect:/admin/users?type="+type;
    }

    @GetMapping("/all-orders")
    public String getAllOrders(Model model, @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "5") Integer pageSize) {

        Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
        model.addAttribute("orders", page.getContent());
        model.addAttribute("srch", false);

        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElement", page.getTotalElements());
        model.addAttribute("totalPage", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/all_orders";
    }

    @PostMapping("/update-status")
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

        return "redirect:/admin/all-orders";
    }

    @GetMapping("searchOrder")
    public String searchOrder(@RequestParam String orderId, Model model, HttpSession session,
            @RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "5") Integer pageSize) {

        if (orderId != null && orderId.length() > 0) {
            ProductOrder orderById = orderService.getOrderById(orderId.trim());

            if (ObjectUtils.isEmpty(orderById)) {
                session.setAttribute("errorMsg", "Order Not Found");
            } else {
                model.addAttribute("order", orderById);
            }
            model.addAttribute("srch", true);
        } else {
            Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
            model.addAttribute("orders", page.getContent());
            model.addAttribute("srch", false);

            model.addAttribute("pageNo", page.getNumber());
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("totalElement", page.getTotalElements());
            model.addAttribute("totalPage", page.getTotalPages());
            model.addAttribute("isFirst", page.isFirst());
            model.addAttribute("isLast", page.isLast());

        }
        return "admin/all_orders";
    }

    @GetMapping("/add-admin")
    public String loadAddAdmin() {
        return "admin/add_admin";
    }

    // @PostMapping("/saveAdmin")
    // public String saveAdmin(@ModelAttribute User user, HttpSession session) {
    // User saveAdmin = userService.saveAdmin(user);

    // if (!ObjectUtils.isEmpty(saveAdmin)) {
    // session.setAttribute("successMsg", "Admin Added Successfully");
    // } else {
    // session.setAttribute("errorMsg", "Some Error Occure");
    // }
    // return "redirect:/admin/add-admin";
    // }


    @GetMapping("/profile")
    public String loadProfile(){
        return "admin/profile";
    }

    @PostMapping("/updateProfile")
    public String updateProfile(@ModelAttribute User user, HttpSession session) {

        User updatUserProfile = userService.updatUserProfile(user);

        if (ObjectUtils.isEmpty(updatUserProfile)) {
            session.setAttribute("errorMsg", "Profile Not Updated");
        } else {
            session.setAttribute("successMsg", "Profile Updated Successfully");
        }

        return "redirect:/admin/profile";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword,
            Principal principal, HttpSession session) {

        User loggedUser = commonUtil.getLoggedUser(principal);

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

        return "redirect:/admin/profile";
    }

}
