package com.ypat.controller;

import com.ypat.ProductQo;
import com.ypat.entity.Product;
import com.ypat.service.ProductService;
import com.ypat.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/service/product/get")
    public ProductQo get(Long id) {
        return productService.findById(id);
    }

    @PostMapping("/service/product/add")
    public void add(@RequestBody ProductQo productQo){
        productService.save(productQo);
    }

    @PostMapping("/service/product/upDown")
    public void upDown(@RequestBody ProductQo productQo){
        productService.upDown(productQo);
    }

    @PostMapping("/service/product/findPage")
    public Map<String, Object> findPage(@RequestBody ProductQo productQo) {
        return productService.findPage(productQo);
    }
}
