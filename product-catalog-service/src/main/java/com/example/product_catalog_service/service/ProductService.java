package com.example.product_catalog_service.service;


//Persistence
import com.example.product_catalog_service.dao.intrface.DaoProductInterface;
import com.example.product_catalog_service.dao.implementation.jdbc.DaoProductImplementationJdbc;

import com.example.product_catalog_service.domainobjectmodel.Product;
import com.example.product_catalog_service.exception.DuplicateSkuException;
import com.example.product_catalog_service.exception.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;



@Service
public class ProductService
{
    DaoProductInterface obj_dao_prod_interface;

    //Requried for @InjectMocks
    public ProductService(DaoProductInterface obj_dao_prod_interface)
    { 
        this.obj_dao_prod_interface= obj_dao_prod_interface;
    }

    @Transactional
    public Product createProduct(Product obj_product)
    {
        //empty body -> compiler error
            //missing return statement
        //for TDD purposes
            //red phase
                //must return somethind OR throw
                    //temporary stub
                        //return Optional.empty();

        

          
        //check if obj_product already exists 
        if(obj_dao_prod_interface.findBySkuId(obj_product.getSkuId()).isPresent())
        {
            //not going to insert already existing product  
            System.out.println("Product with skuId: " +obj_product.getSkuId() + "  ALREADY EXISTS so NOT going to insert said object");


            //throw DuplicateSkuException
            throw new DuplicateSkuException(obj_product.getSkuId());
           
        }
        
        //attempt to insert obj_product
        obj_dao_prod_interface.insert(obj_product);

        
        //return object Product instance
        return obj_product;
        

    }

    @Transactional(readOnly=true)
    public Product getProduct(String obj_sku_id)
    {
        //if NOT find Product return said Product
            // return  ProductNotFoundException
        //else return said Product object instance
        if(obj_dao_prod_interface.findBySkuId(obj_sku_id).isEmpty())
        {
            throw  new ProductNotFoundException(obj_sku_id);
        }
        else return  obj_dao_prod_interface.findBySkuId(obj_sku_id).get();
        
        
    }
}