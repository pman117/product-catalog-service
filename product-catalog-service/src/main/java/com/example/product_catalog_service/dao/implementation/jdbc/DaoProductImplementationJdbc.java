package com.example.product_catalog_service.dao.implementation.jdbc;

import com.example.product_catalog_service.domainobjectmodel.Product;
import com.example.product_catalog_service.dao.intrface.DaoProductInterface;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.BeanCreationException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;



@Repository
public class DaoProductImplementationJdbc implements DaoProductInterface,InitializingBean
{

    
    NamedParameterJdbcTemplate objNamedParamJdbcTemp;

    DaoProductImplementationJdbc(NamedParameterJdbcTemplate objNamedParamJdbcTemp)
    {
        this.objNamedParamJdbcTemp= objNamedParamJdbcTemp;
    }


    //(DML (Insert, Delete, Update))
        //implementation by overriden method of DAO interface in the DAO implementation class
            //NamedParameterJdbcTemplate
                //update() method
    @Override
    public int insert(Product obj_product)
    {
        //update() method
            //DML SQL Query Insert
            //SqlParameterSource

        //DML SQL Query Insert
        String objDmlSqlQueryInsert; 
        objDmlSqlQueryInsert= "INSERT INTO product(skuId, productName, price) VALUES(:skuId, :productName, :price)";
        
        //SqlParameterSource
        SqlParameterSource objMapSqlParamSource = new MapSqlParameterSource()
            .addValue("skuId",obj_product.getSkuId() )
            //.addValue("skuId", UUID.randomUUID().toString())
            .addValue("productName", obj_product.getName())
            .addValue("price", obj_product.getPrice());

        //NamedParameterJdbcTemplate + return number of rows affected
            //update() method
        return objNamedParamJdbcTemp.update(objDmlSqlQueryInsert,objMapSqlParamSource);
        
        
        //return 1;
    }


    //(DML(Query)Retrieve 1 single value(row/tupple/record))
        //implementation by overriden method of DAO interface in the DAO implementation class
            // NamedParameterJdbcTemplate
                // queryForObject() method
    
    //RowMapper
        //private final static method
        private final static RowMapper <Product> objRowMapperFinalStaticPriv = new RowMapper<>()
        {
            @Override
            public Product mapRow(ResultSet objResultSet, int rowNum) throws SQLException
            {
                //Product objProd = new Product();
                
                String skuId= objResultSet.getString("skuId");

                String productName= objResultSet.getString("productName");

                java.math.BigDecimal price = objResultSet.getBigDecimal("price");

                Product objProd= new Product(skuId,productName,price);

                return objProd;

            }
        };
    
    @Override
    public Optional<Product>findBySkuId(String obj_sku_id)
    {
        
        //queryForObject() method 
            //SqlParameterSource
            //RowMapper


        //DML(Query)Retrieve 1 single value(row/tupple/record) 
        String objDmlQueryForObject;
        objDmlQueryForObject= "SELECT * From product WHERE skuId= :skuId";   

        //SqlParameterSource
        SqlParameterSource objMapSqlParamSource2 = new MapSqlParameterSource()
            .addValue("skuId", obj_sku_id);


        

        try
        {
            //check
                //print
                Product objProdFindBySkuId = objNamedParamJdbcTemp.queryForObject(objDmlQueryForObject,objMapSqlParamSource2,objRowMapperFinalStaticPriv);

                System.out.println("objProdFindBySkuId.getSkuId(): "+objProdFindBySkuId.getSkuId());
        
            //return Optional.ofNullable( objNamedParamJdbcTemp.queryForObject(objDmlQueryForObject,objMapSqlParamSource2,objRowMapperFinalStaticPriv));
            return Optional.ofNullable(objProdFindBySkuId);
        }
        catch(EmptyResultDataAccessException objRunntimeUncheckedEmptyResultDataAccessException)
        {
            return Optional.empty();
        }
        




       //Optional<Product> obj_optional= Optional.empty();

       // obj_optional;
    }                                               

    //override afterPropertiesSet() from InitializingBean interface
        //invoked by the containing BeanFactory after it has set all bean properties + satisfied BeanFactoryAware,ApplicationContextAware
            //allows bean/object/component instance to perform validation of its overall configuration + final initialization when all bean properties have been set
    
    @Override
    public void afterPropertiesSet()
    {
        if (objNamedParamJdbcTemp == null)
        {
            throw new BeanCreationException("Bean/component/object NamedParameterJdbcTemplate objNamedParamJdbcTemp instance is null");
        }
    }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
}