package com.example.product_catalog_service.dao.intrface;

import com.example.product_catalog_service.domainobjectmodel.Product;
import java.util.List;
import java.util.Optional;

public interface DaoProductInterface
{
    int insert(Product obj_product);

    Optional<Product>findBySkuId(String obj_sku_id);
}