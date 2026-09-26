package com.example.product_catalog_service.exception;

public class DuplicateSkuException extends RuntimeException
{
    public DuplicateSkuException(String obj_sku_id)
    {
        super("Product with said skuId already exists: "+ obj_sku_id);
    }
}
