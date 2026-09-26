package com.example.product_catalog_service.domainobjectmodel;

import java.math.BigDecimal;

public class Product
{
	private String skuId;
	
	private String productName;
	
	private BigDecimal price;

	
	
	public Product(String skuId,String productName,BigDecimal price)
	{
		this.skuId= skuId;
		
		this.productName= productName;
		
		this.price = price;
		
		if(skuId == null)
		{
			IllegalArgumentException obj_illegal_argument_exception= new IllegalArgumentException("SKU_ID is null");
			throw obj_illegal_argument_exception;
		}
		if(skuId == "")
		{
			throw new IllegalArgumentException("skuId is an EMPTY String");
		}
		if(skuId == " ")
		{
			throw new IllegalArgumentException("skuId is blank white space");
		}
		if(skuId.contains("#" ))
		{
			throw new IllegalArgumentException("skuId has an illegal character");
		}
		if(productName == null)
		{
			throw new IllegalArgumentException("productName is null");
		}
		if(productName == "")
		{
			throw new IllegalArgumentException("productName is an empty String");
		}
		if(productName == " ")
		{
			throw new IllegalArgumentException("productName is a blank whitespace");
		}
		if(productName.length()> 25)
		{
			throw new IllegalArgumentException("productName length > 25 characters");
		}
		if(price == null)
		{
			throw new IllegalArgumentException("price is null");
		}
		if(price.compareTo(BigDecimal.valueOf(0))==0)
		{
			//System.out.println("price == BigDecimal.valueOf(0)");
			//System.out.println(price);
			throw new IllegalArgumentException("price is 0: " + price);
		}
		if(price.compareTo(BigDecimal.valueOf(Long.MAX_VALUE))==1)
		{
			//System.out.println(price);
			throw new IllegalArgumentException("price > BigDecimal.valueOf(Long.MAX_VALUE): " + price);
		}
		if(price.compareTo(BigDecimal.valueOf(0))==-1)
		{
			//System.out.println(price);
			throw new IllegalArgumentException("price < 0: " + price);
		}
		
		
	}
	
	public String getSkuId()
	{
		return skuId;
	}
	
	public String getProductName()
	{
		return productName;
	}
	
	public BigDecimal getPrice()
	{
		return price;
	}
	
	
	
}