package com.example.product_catalog_service.exception;

public class ProductNotFoundException  extends RuntimeException
{
   public  ProductNotFoundException(String sku_id)
    {
        super("Product with said skuId does NOT exist: "+ sku_id);
    }
}

