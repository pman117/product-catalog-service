package com.example.product_catalog_service.controller;

import com.example.product_catalog_service.exception.DuplicateSkuException;
import com.example.product_catalog_service.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler 
{

    @ExceptionHandler(DuplicateSkuException.class)
    @ResponseStatus(HttpStatus.CONFLICT)   //409
    public String handleDuplicateSku(DuplicateSkuException obj_exp_duplicate_sku)
    {
        return obj_exp_duplicate_sku.getMessage();
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) //404
    public String handleProductNotFound(ProductNotFoundException obj_exp_prod_not_found)
    {
        return obj_exp_prod_not_found.getMessage();
    }



}
