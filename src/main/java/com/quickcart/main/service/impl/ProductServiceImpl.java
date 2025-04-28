package com.quickcart.main.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.quickcart.main.model.Product;
import com.quickcart.main.repository.ProductRepository;
import com.quickcart.main.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);

    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Boolean deleteProduct(int id) {
        Product product = productRepository.findById(id).orElse(null);

        if (!ObjectUtils.isEmpty(product)) {
            productRepository.delete(product);
            return true;
        }
        return false;
    }

    @Override
    public Product getProductById(int id) {
        Product product = productRepository.findById(id).orElse(null);
        return product;
    }

    @Override
    public Product updateProduct(Product product, MultipartFile file) {
        Product oldProduct = getProductById(product.getId());

        String imageName = file.isEmpty() ? oldProduct.getImage() : file.getOriginalFilename();

        oldProduct.setTitle(product.getTitle());
        oldProduct.setDescription(product.getDescription());
        oldProduct.setCategory(product.getCategory());
        oldProduct.setPrice(product.getPrice());
        oldProduct.setIsActive(product.getIsActive());
        oldProduct.setStock(product.getStock());
        oldProduct.setImage(imageName);
        oldProduct.setDiscount(product.getDiscount());

        Double updateDiscount = product.getPrice() * (product.getDiscount() / 100.0);

        oldProduct.setDiscountPrice(product.getPrice() - updateDiscount);

        Product updateProduct = productRepository.save(oldProduct);

        if (!ObjectUtils.isArray(updateProduct)) {
            if (file.isEmpty()) {
                try {
                    File saveFile = new ClassPathResource("static/img").getFile();
                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "Products" + File.separator
                            + file.getOriginalFilename());

                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return product;
        }
        return null;
    }

    @Override
    public List<Product> getAllActiveProducts(String category) {

        List<Product> productList = null;
        if (ObjectUtils.isEmpty(category)) {
            productList = productRepository.findByIsActiveTrue();
        } else {
            productList = productRepository.findByCategory(category);
        }
        return productList;
    }

    @Override
    public List<Product> searchProduct(String query) {
        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(query, query);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page pageProduct = null;

        if (ObjectUtils.isEmpty(category)) {
            pageProduct = productRepository.findByIsActiveTrue(pageable);
        } else {
            pageProduct = productRepository.findByCategory(pageable, category);
        }
        return pageProduct;
    }

    @Override
    public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String query) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(query,
                query, pageable);

    }

    @Override
    public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category,String ch) {
        Page<Product> pageProduct = null;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        pageProduct = productRepository.findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
        
        // if (ObjectUtils.isEmpty(category)) {
        //     pageProduct = productRepository.findByIsActiveTrue(pageable);
        // } else {
        //     pageProduct = productRepository.findByCategory(pageable, category);
        // }
        return pageProduct;
    }

    

    

}
