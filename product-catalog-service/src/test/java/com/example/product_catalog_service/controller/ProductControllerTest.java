package com.example.product_catalog_service.controller;

import com.example.product_catalog_service.controller.ProductController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.databind.ObjectMapper;
// import com.example.product_catalog_service.controller.dto.CreateProductRequest;
import com.example.product_catalog_service.domainobjectmodel.Product;
import com.example.product_catalog_service.exception.DuplicateSkuException;
import com.example.product_catalog_service.exception.ProductNotFoundException;
import com.example.product_catalog_service.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.lang.IllegalArgumentException;


//What a Controller Test IS at the Mechanical Level
    //3 Things Every Controller Test must prove(Thing to prove -> What it means -> How MockMvc proves it)
        //1)Internet -WEB HTTP Application Layer Contract
            //Given this Internet- WEB Http Application Layer Request shape(method, path, body, headers) --> the said Controller returns HTTP Response shape(status code, JSON body)
                //perform(post("/api/v1/products").contentType(JSON).content(body)) then andExpect(status().isCreated()
            
        //2)Delegation Contract
            // the said Controller calls the correct service method with the correct arguments-nothing more
                //verify(obj_mock_prod_service).createProduct(any(Product.class))

        //3)Exception Mapping
            //Exception Mapping
                //When the service throws a domain exception-> the said Controller returns the correct HTTP error status via GlobalExceptionHandler
                    //when(service.method()).thenThrow(SomeException) then andExpect(status().isNotFound())

//@WebMvcTest -ProductControllerTest
    //loads ONLY 
         //DispatcherServlet wired
         //said Controller class
            //ProductController.class
                //
         //GlobalExceptionHandler laoded automatically
         //HandlerMapping + HandlerAdapter
         //MockMvc
         //Jackson ObjectMapper autoconfigured
         //Validator + Valid autoconfigured
         //@Autowired MockMvc obj_mock_mvc
            //Spring autoconfigured MockMvc 
                //because @WebMvcTest is active
                    //MockMvc is wired with
                        //DispatcherServlet
                        //ProductController(Spring instatntiated this, injecting @MockBean)
                        //GlobalExceptionHandler(loaded because @RestControllerAdvice is in the slice)
                        //Jackson ObjectMapper(autoconfigured)
                        //Validator(autoconfigured)
         //stubs the SERVICE(ProductController's dependency)
            //when obj_mock_prod_service.createProduct(any(Product.class)).thenReturn(obj_prod)
                //ProductController calls the said Service class -> so you stub the said Service class
                    //Unit under test(UUT)= ProductController
                    //Dependency being replaced = ProductService
                    //Pattern
                        //Always  stub the DIRECT dependency of the unit under test
                            //ProductControllerTest NEVER stubs the DAO- it does NOT care about the DAO
                            //ProductServiceTest NEVER stubs the Internet WEB HTTP layer- it does NOT care about the Internet Web HTTP Application layer
            //@MockBean creates proxy AND registers in Spring context
            //Bytebuddy creates proxy -> stores in Mockito's registry
         //Tests
            //full HTTP Application Layer level Request<-> Response pipeline in process
        //Speed
            //1-2s per  test class startup
        //Use for
            //Controllers
            //ExceptionHandlers -HTTP Contract
        
         //NO
            //DataSource
            //DAO
            //Docker
    //4 Phases
        //1) Proxy Creation
            //@MockBean ProductService -> ByteBuddy creates runtime proxy -> Implements ProductService-> Registered as Spring concept/component/object/bean-> Spring wires(DI) into said Controller ProductController constructor -> 
            //@Autowired MockMvc -> Spring auto configures MockMvc-> wired with DispatcherServelet + said controller-> ready to dispatch HTTP requests in process

        //2) Stubbing Behavior
            //when(obj_mock_prod_service.createProduct(any(Product.class))).thenReturn(obj_product)
                //defines what ProductService mock returns
                    //when said ProductController calls it
                        //happens BEFORE perform -sets up the response the said controller ProductController will get the said service ProductService
        
        //3) Invocation Recording
            // happens AUTOMATICALLY triggered  through Internet Web  HTTP dispatch NOT by direct Java method call as in ProductServiceTest
                //when mockMvc.perform(post("/api/v1/products")...)
                    //1.MockMvc builds MockHttpServletRequest from said specs->
                        //2.DispathcerServlet.service() is called (same as real Tomcat)->
                            //3.HandlerMapping : POST /api/v1/products-> 
                                    //ProductController.createProduct()
                                        //4.ArgumentResolver: sees @RequestBody  -> Jackson reads JSON bytes-> deserialization
                                            //5.ValidatorAdaptor sees @Valid on the parameter(Runs @NotBlank on skuId → passes ("ABC-123" is not blank)',
                                                //→ Runs @NotBlank on productName → passes',
                                                //→ Runs @DecimalMin on price → passes (95 > 0.01)')
                                            //6.Controller method body executes
                                                //Product obj_prod = new Product() -> builds domain model data object
                                                    //obj_prod_service.createProduct() -> calls the mock
                                            //7.Mockito intercepts createProduct(obj_prod)
                                                //Records: method=createProduct, arg=Product("ABC-123","widget",95)
                                                    //looks up stub registry-> finds the when().thenReturn() from phase 2
                                                        //Returns obj_prod(the stubbed return value)
                                            //8.Controller wraps in ApiResponse.ok(ProductResponse.from(obj_prod))
                                            //9.@ResponseStatus(CREATED) sets  HTTP 201
                                            //10.@ResponseBody(from @RestController) triggers Jackson-> 
                                                //serializes ApiResponse<ProductResponse> to JSON bytes-> 
                                                    //writes to   MockHttpServletResponse
                                                
                        //@RequestBody -> deserializes  JSON bytes -> Product -> @Valid runs concept/bean/component/object validation
                            //said controller ProductController calls obj_mock_bean_prod_service.createProduct(product)-> Mockito intercepts + records said call-> returns stubbed product-> 
                                //Jackson -> serializes Product -> JSON bytes

        //4)Post Execution Verification(asserts on HTTP Application Layer Response(status + JSON body) NOT java objects as in ProductServiceTest    )
            //Part A
                //MockMvc matchers on Internet WEB HTTP Application Layer Response
                    //.andExpect(status().isCreated())
                    //.andExpect(jsonPath("$.success").value(true))
                    //.andExpect(jsonPath("$.data.skuId").value("ABC"))
            //Part B
                //Mockito interaction verification which BOTH happen inside same test method chain
                    //verify(obj_mock_prod_service).createProduct(any(Product.class))     
                    
                    

//@ExtendWith(MockitoExtension)- ProductServiceTest
    //0 Spring context loaded
    //NO Dispatcher Servlet
    //NO HTTP Routing
    //NO Jackson Serialization
    //NO @Valid enforcement
    //Mockito wires mocks directly via constructor
    //@Mock
        //creates proxy
    //@InjectMock
        //creates SUT
    //Tests
        //pure Java method calls in/out
    //Speed
        //50ms per test startup
    //Use for
        //domain
        //service
        //DAO
            //NO HTTP Application Layer 
    //4 Phases
        //1) Proxy Creation
//@MockBean
    //critical DISTINCTION from @Mock
        //@MockBean creates a Mockito proxy + registers it in the Spring ApplicationContext
            //Spring then wires it into said Controller class's constructor (ProductController.java)
                //@MockBean ProductService
        //@Mock alone
            //is INVISIBLE to Spring 
                //Context canNOT start withOUT a real ProductService bean/component/object/concept

@WebMvcTest(ProductController.class)
class ProductControllerTest
{
    //1)ProxyCreation
    @MockBean ProductService obj_mock_prod_service;
    @Autowired MockMvc obj_mock_mvc;
    @Autowired ObjectMapper obj_object_mapper;

    @Test 
    void createProductReturns201WhenValid() throws Exception
    {
        //arrange -build test data(NOT a Mockito phase BUT test setup)
            //DTO or Domain Data Model Product Object -> serialized -> JSON -> sent as HTTP body
                //simulates what a real client(Postman, REST Assured, Angular,React,etc..) would send
            //Domain Data Model objec the mocked service will  return
                //IF DTO -> 
                    //the said controller ProductController converts it to a ProductResponse DTO BEFORE returning
        Product obj_prod = new Product("ABC-122","widget", BigDecimal.valueOf(95));

        //2)Stubbing Behavior
            //Mockito returns a stub in its internal registry
                //IF createProduct is called with ANY Product object argument(we canNOT get a reference to that internal Product object from the test)
                    //return obj_prod
        when(obj_mock_prod_service.createProduct(any(Product.class))).thenReturn(obj_prod);

        //3) Invocation Recording 
        obj_mock_mvc.perform(post("/api/v1/products")
            .contentType(MediaType.APPLICATION_JSON) //Content-Type Header
            .content(obj_object_mapper.writeValueAsString(obj_prod))) //serializes into raw JSON bytes sent as the HTTP Request body CreateProductRequest or Product object -> JSON string-> {"skuId":"ABC-123","productName":"widget","price":95}

        //4) Post execution verification
            //Part A
                //MockMvc matchers on Internet-WEB HTTP Application Layer Response
            .andExpect(status().isCreated()) //Asserts HTTP response status= 201 created + verifies:@ResponseStatus(HttpStatus.CREATED) on createProduct()
            //.andExpect(jsonPath("$.success").value(true)) 
            //jsonPath("$.success") navigates the response JSON: 
                //$ = root of JSON  object
                //.success= field of success
                //.value(true)= asserts its value  is boolean true
                //Verifies:ApiResponse.ok() set success=true
            
            .andExpect(jsonPath("$.skuId").value("ABC-122"))
            //$.data = the "data" field of ApiResponse<ProductResponse>
            //.skuId = the "skuId" field of ProductResponse
            //Verifies: ProductResponse.from(product) correctly mapped skuId

            .andExpect(jsonPath("$.productName").value("widget"))
            .andExpect(jsonPath("$.price").value(95.0));
            //price 95 (BigDecimal) -> JSON number 95 -> Java double 95.0 for comparison

            //Part B
                //Mockito interaction verification

        verify(obj_mock_prod_service).createProduct(any(Product.class));
    }

    @Test
    void createProductReturns409WhenSkuAlreadyExists() throws Exception
    {
        //arrange -build test data(NOT a Mockito phase BUT test setup)
            //DTO or Domain Data Model Product Object -> serialized -> JSON -> sent as HTTP body
                //simulates what a real client(Postman, REST Assured, Angular,React,etc..) would send
            //Domain Data Model objec the mocked service will  return
                //IF DTO -> 
                    //the said controller ProductController converts it to a ProductResponse DTO BEFORE returning
                    Product obj_prod = new Product("ABC-122","widget", BigDecimal.valueOf(95));

                    //2)Stubbing Behavior
                        //Mockito returns a stub in its internal registry
                            //IF createProduct is called with ANY Product object argument(we canNOT get a reference to that internal Product object from the test)
                                //throw DuplicateSkuException
                    when(obj_mock_prod_service.createProduct(any(Product.class)))
                            .thenThrow(new DuplicateSkuException("ABC-122"));
            
                    //3) Invocation Recording 
                    obj_mock_mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON) //Content-Type Header
                        .content(obj_object_mapper.writeValueAsString(obj_prod))) //serializes into raw JSON bytes sent as the HTTP Request body CreateProductRequest or Product object -> JSON string-> {"skuId":"ABC-123","productName":"widget","price":95}
            
                    //4) Post execution verification
                        //Part A
                            //MockMvc matchers on Internet-WEB HTTP Application Layer Response
                        .andExpect(status().isConflict()); //Asserts HTTP response status=  Conflict -> there is a 400 level Client error on endpoint/service/resource
                        
            
                        //Part B
                            //Mockito interaction verification

                            //verify(obj_mock_prod_service, never()).createProduct(any(Product.class));
                        
    }

    @Test
    void createProductReturns400WhenSkuIdIsBlank() throws Exception
    {
        //arrange -build test data(NOT a Mockito phase BUT test setup)
            //DTO or Domain Data Model Product Object -> serialized -> JSON -> sent as HTTP body
                //simulates what a real client(Postman, REST Assured, Angular,React,etc..) would send
            //Domain Data Model objec the mocked service will  return
                //IF DTO -> 
                    //the said controller ProductController converts it to a ProductResponse DTO BEFORE returning
                    //Do NOT construct new Product("",...)
                        //constructor rejects it immediately
                            //send RAW JSON string directly -let  the HTTP pipeline handle rejection
                                //when Jackson tries to deserialize bytes JSON -> object Product
                                    //constructor throws IllegalArgumentException -> Spring  -> HttpMessageNotReadableException-> 400
                                        String obj_str_JSON_invalid_body ="""
                                                 {"skuId": "", "productName": "widget" , "price":95}
                                                """;
                                            
                                        // Product obj_prod = new Product("","widget", BigDecimal.valueOf(95));

                    //2)Stubbing Behavior
                        //NO Stub needed
                            //the service mock is never called when validation fails at deserialization
                      //when(obj_mock_prod_service.createProduct(any(Product.class))).thenThrow(new IllegalArgumentException());
            
                    //3) Invocation Recording 
                    obj_mock_mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON) //Content-Type Header
                        .content(obj_object_mapper.writeValueAsString(obj_str_JSON_invalid_body))) //serializes into raw JSON bytes sent as the HTTP Request body CreateProductRequest or Product object -> JSON string-> {"skuId":"ABC-123","productName":"widget","price":95}
            
                    //4) Post execution verification
                        //Part A
                            //MockMvc matchers on Internet-WEB HTTP Application Layer Response
                        .andExpect(status().isBadRequest()); //Asserts HTTP response status code HttpStatus.BAD_REQUEST (400) there is a 400 level Client error on endpoint/service/resource
                        
            
                        //Part B
                            //Mockito interaction verification
                            verify(obj_mock_prod_service, never()).createProduct(any(Product.class));
    }

    @Test
    void getRetrieveProductReturns200WhenSkuIdFound() throws Exception
    {
        //arrange -build test data(NOT a Mockito phase BUT test setup)
            //DTO or Domain Data Model Product Object -> serialized -> JSON -> sent as HTTP body
                //simulates what a real client(Postman, REST Assured, Angular,React,etc..) would send
            //Domain Data Model objec the mocked service will  return
                //IF DTO -> 
                    //the said controller ProductController converts it to a ProductResponse DTO BEFORE returning       
                        Product obj_prod = new Product("ABC-122","widget", BigDecimal.valueOf(95));

                    //2)Stubbing Behavior
                        
                            //the service mock is  called then returns said object obj_prod
                      when(obj_mock_prod_service.getProduct("ABC-122")).thenReturn(obj_prod);
            
                    //3) Invocation Recording 
                    obj_mock_mvc.perform(get("/api/v1/products/{skuId}", "ABC-122"))
                        
                    //4) Post execution verification
                        //Part A
                            //MockMvc matchers on Internet-WEB HTTP Application Layer Response
                        .andExpect(status().isOk()); //Asserts HTTP response status code HttpStatus.OK (200) there is success on endpoint/service/resource
                        
            
                        //Part B
                            //Mockito interaction verification
                            verify(obj_mock_prod_service, times(1)).getProduct(any(String.class));
    }

    @Test
    void getRetrieveProductReturns404WhenSkuIdNotFound() throws Exception
    {
        //arrange -build test data(NOT a Mockito phase BUT test setup)
            //DTO or Domain Data Model Product Object -> serialized -> JSON -> sent as HTTP body
                //simulates what a real client(Postman, REST Assured, Angular,React,etc..) would send
            //Domain Data Model objec the mocked service will  return
                //IF DTO -> 
                    //the said controller ProductController converts it to a ProductResponse DTO BEFORE returning       
                    Product obj_prod = new Product("ABC-122","widget", BigDecimal.valueOf(95));

                    //2)Stubbing Behavior
                     
                            //the service mock is called then
                      when(obj_mock_prod_service.getProduct("ABC-122")).thenThrow(new ProductNotFoundException("ABC-122"));
            
                    //3) Invocation Recording 
                    obj_mock_mvc.perform(get("/api/v1/products/{skuId}", "ABC-122"))
                        
                    //4) Post execution verification
                        //Part A
                            //MockMvc matchers on Internet-WEB HTTP Application Layer Response
                        .andExpect(status().isNotFound()); //Asserts HTTP response status code HttpStatus.NOT_FOUNd (404) there is a client error endpoint/service/resource
                        
            
                        //Part B
                            //Mockito interaction verification
                            //verify(obj_mock_prod_service, times(1)).getProduct(any(String.class));
    }
    
    



}