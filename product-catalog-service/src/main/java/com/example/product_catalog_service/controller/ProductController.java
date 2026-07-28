package com.example.product_catalog_service.controller;

//import com.example.product_catalog_service.controller.dto.CreateProductRequest;
//import com.example.product_catalog_service.controller.dto.ProductResponse;
import com.example.product_catalog_service.domainobjectmodel.Product;
import com.example.product_catalog_service.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional; 

@RestController
@RequestMapping("/api/v1/products")
public class ProductController 
{
    //Logger
    static final private Logger obj_logger= LoggerFactory.getLogger(ProductController.class);


    //ProductService object
    ProductService obj_prod_service;

    ProductController(ProductService obj_prod_service)
    {
        this.obj_prod_service= obj_prod_service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Product createProduct(@Valid @RequestBody Product obj_prod)
    {
        //implement(define + declare + instantiate+ initialize new Product object obj_prod)
        //Product obj_prod= new Product(obj_prod.getSkuId(), obj_prod.getProductName(), obj_prod.getPrice());

        //call service with said Product object
        Product obj_prod_service_call= obj_prod_service.createProduct(obj_prod);

        //Logging
        obj_logger.info("POST /api/v1/products skuId={} productName={}  price={}", obj_prod.getSkuId(), obj_prod.getProductName(), obj_prod.getPrice());

        //return Product object which called service
        return  obj_prod_service_call;
    }

    //@GetMapping with NO arguement 
        //maps to GET /api/v1/products
            //this a GET ALL endpoint/resource/servide NOT 1
                //when MockMvc sends request GET /api/v1/products/ABC-122
                    // Spring finds NO handler for that said path
                        //fails through static resource handler -> 404
        //@GetMapping WITH argument
            //@GetMapping("/{obj_prod_sku_id}")
                //path template must match the @PathVariable
    @GetMapping("/{obj_prod_sku_id}")
    Product getProduct(@PathVariable String obj_prod_sku_id)
    {

        //call the service with said  skuId
        Product obj_prod_service_call= obj_prod_service.getProduct(obj_prod_sku_id);

        //Logging
        obj_logger.info("GET /api/v1/products/{skuId} skuId={}", obj_prod_sku_id);

        return obj_prod_service_call;


        //Product obj_prod = new Product("ABC-122","widget", BigDecimal.valueOf(95));
        //return obj_prod;

        
    }
}
