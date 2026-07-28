package com.example.product_catalog_service.domainobjectmodel;

import com.example.product_catalog_service.domainobjectmodel.Product;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductTests
{
	//define + declare object/reference/instance variable
	private Product obj_product_test;
	
	@BeforeAll
	static void beforeAll()
	{
		//start expensive resources if needed
		//implement immutable template
			//implement IF needed a ProductDataFieldBuilder class IF MANY fields
			//1 time ONLY to AVOID duplication
			//EACH test class clones the said template to guarantee isolation on mutable state 
		
		//OR AVOID
			//RELATIVELY slight duplication
				//BUT RELATIVE absolute clarity
			//RELATIVLEY optimal for TDD
	}
	
	@BeforeEach
	void beforeEach()
	{
		//nothing heavy here
		//reset mutable state
		//create fresh mutable instances in each test for isolation
	}
	
	@AfterEach
	void afterEach()
	{
		//no operation
		//reset mocks
		//close RELATIVELY small resources
			//RARELY pure domain tests
		//verify no global side effects
	}
	
	@AfterAll
	static void afterAll()
	{
		//tear down IF ANY shared RELATIVELY heavy resources
			//none here
	}
	
	//inputs-> input-> input domain-> equivalence partition classes  :1)valid 2)boundary 3)invalid
		//inputs
			//skuId 
			//productName
			//price
			//createdAt
			//Discount_Percent
	
	@Test
	void inputDomainEquivalencePartitionClassValid()
	{
		//create fresh mutable instances in each test for isolation
			//instantiate + initialize Product object
				obj_product_test= new Product("ABC-123", "widget",BigDecimal.valueOf(95));
				
		//assertAll to group checks
			assertAll("inputDomainEquivalencePartitionClassValid",
						()-> assertEquals("ABC-123",obj_product_test.getSkuId()),
						()-> assertEquals("widget",obj_product_test.getProductName()),
						()-> assertEquals(BigDecimal.valueOf(95),obj_product_test.getPrice())
					);
				
	}
	
	@Test
	void inputDomainEquivalencePartitionClassBoundaryLow()
	{
		//create fresh mutable instances in each test for isolation
		//instantiate + initialize Product object
			obj_product_test= new Product("A1", "a",BigDecimal.valueOf(0.01));
			
		
		assertAll("inputDomainEquivalencePartitionClassBoundaryLow",
					()-> assertEquals("A1", obj_product_test.getSkuId()),
					()-> assertEquals("a", obj_product_test.getProductName()),
					()-> assertEquals(BigDecimal.valueOf(0.01), obj_product_test.getPrice())
				
				);
	}
	
	@Test
	void inputDomainEquivalencePartitionClassBoundaryHigh()
	{
		//create fresh mutable instances in each test for isolation
		//instantiate + initialize Product object
			obj_product_test= new Product("ABC-999", "a".repeat(25),BigDecimal.valueOf(Long.MAX_VALUE));
		
		assertAll("inputDomainEquivalencePartitionClassBoundaryHigh",
					()-> assertEquals("ABC-999", obj_product_test.getSkuId()),
					()-> assertEquals("a".repeat(25), obj_product_test.getProductName()),
					()-> assertEquals(BigDecimal.valueOf(Long.MAX_VALUE), obj_product_test.getPrice())
				
				);
	}
	
	@Test
	void inputDomainEquivalencePartitionClassInvalid()
	{
		assertAll("inputDomainEquivalencePartitionClassInvalid",
					()-> assertThrows(IllegalArgumentException.class,()-> new Product(null,"widget",BigDecimal.valueOf(95)),"skuId is null"),
					()-> assertThrows(IllegalArgumentException.class,()-> new Product("","widget",BigDecimal.valueOf(95)),"skuId is empty String"),
					()-> assertThrows(IllegalArgumentException.class,()-> new Product(" ","widget", BigDecimal.valueOf(95)),"skuId is blank white space"),
					()-> assertThrows(IllegalArgumentException.class,()-> new Product("abc#123","widget", BigDecimal.valueOf(95)),"skuId has illegal characters"),
					()-> assertThrows(IllegalArgumentException.class,()-> new Product("ABC-123",null, BigDecimal.valueOf(95)),"productName is null"),
					()-> assertThrows(IllegalArgumentException.class,()-> new Product("ABC-123","", BigDecimal.valueOf(95)),"productName is empty String"),
					()-> assertThrows(IllegalArgumentException.class,()-> new Product("ABC-123"," ", BigDecimal.valueOf(95)),"productName is blank empty whitespace"),
					()-> assertThrows(IllegalArgumentException.class,()-> new Product("ABC-123","a".repeat(26), BigDecimal.valueOf(95)),"Length of productName is > 25 characters"),
					()-> assertThrows(IllegalArgumentException.class,()-> new Product("ABC-123","widget", BigDecimal.valueOf(0)),"price is 0"),
					()-> assertThrows(IllegalArgumentException.class,()-> new Product("ABC-123","widget", null),"price is null"),
					()-> assertThrows(IllegalArgumentException.class,()-> new Product("ABC-123","widget", BigDecimal.valueOf(-11)),"price < 0")
				
				
				
				
				);
	}
	
	
	
	
	
	

}


