package com.ypat.controller;

import com.ypat.ProductQo;
import com.ypat.service.ProductServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {
    private static Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductServiceClient productServiceClient;

    @GetMapping("/product/get")
    public String get(Long id) {
        return productServiceClient.get(id);
    }

    @PutMapping("/product/add")
    public String add(ProductQo productQo) {
        return productServiceClient.add(productQo);
    }

    @GetMapping("/product/list")
    public String findPage(ProductQo productQo) {
        return productServiceClient.findPage(productQo);
    }

}