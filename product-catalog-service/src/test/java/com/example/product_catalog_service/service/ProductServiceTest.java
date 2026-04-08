
// ── JUnit 5 ──────────────────────────────────────────────────────────────────
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

// ── Mockito ───────────────────────────────────────────────────────────────────
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

// ── Domain ────────────────────────────────────────────────────────────────────
import com.example.product_catalog_service.domainobjectmodel.Product;

import com.example.product_catalog_service.exception.DuplicateSkuException;
import com.example.product_catalog_service.exception.ProductNotFoundException;

//Persistence
import com.example.product_catalog_service.dao.intrface.DaoProductInterface;
import com.example.product_catalog_service.dao.implementation.jdbc.DaoProductImplementationJdbc;

//Service
import com.example.product_catalog_service.service.ProductService;

// ── Java ──────────────────────────────────────────────────────────────────────
import java.math.BigDecimal;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)         // ← class-level: activates @Mock/@InjectMocks
class ProductServiceTest
{
    
    //WHY mocks exist
    //tests should ONLY fail when unit's behavior regresses
        //NOT because external systems(DB,HTTP, time) are slow or flaky
    //Mock tests isolate the unit under test(UUT)
        //so you get RELATIVELY VERY fast, deterministic feedback for source code that has business logic or orchestration responsibilities
    //use Mocks to verify interaction contracts(did the service call DAO as expected, etc..)
        //and stubs to provide deterministic input to the SUT(System Under Test)
    //Where to use Mocks in said project
        //Controller
        //Service
            //Use ExtendWith(MockitoExtension.class)
                //with @Mock for DAO
            //OR @MockBean if using Spring context
            //Unit test
                // business rules
                // error paths
                // transaction orchestration
            //verify interactions
                //verify insert called when appropriate and stub responses
        //DAO(Persistence) Layer
        //Domain
            //do NOT use mock
                //INSTEAD 
                    //pure unit tests for validation and invariants
        //Elements which you will use repeatedly
            //@ExtendWith(MockitoExtension.class) plain unit tests(no Spring)
            //@Mock and @InjectMocks
                //classic Mockito injection
            //@WebMvcTest(Controller.class) + @MockBean- controller slice with Spring MVC and mock service
            //MockMvc for driving controller requests
            //@SpringBootTest + @MockBean when you need a full app context but want to replace some beans
            //ArgumentCaptor- assert when passed into the mock
            //doReturn/when/doThrow- stubbing
            //doAnswer- dynamic stubbing(callback)
            //verify(...)method(...)- assert interactions
            //verifyNoMoreInteractions(mock)-ensure no unexpected calls
            //spy ()- partial mocking, use rarely
            //MockK if you use Kotlin
            //Best practices/anti patterns
                //Do:
                    //Use constructor injection in your Spring beans(makes mocking/injection straightforwad)
                    //Keep controller tests focused on HTTP contract and delegate business checks to service tests
                    //Use small,readable test names
                    //Use a test data builder for commonly used domain objects
                //Avoid:
                    //Over-mocking(mock everything in @SpringBootTest)
                        //defeats the point
                    //Verifying internal private method calls
                    //Mocking the thing you actually want to test
                        // don't mock DAO in DAO unit tests
            //Common pitfalls in  mock tests and how to avoid them
                //over-verifying internal calls
                    //ONLY verify interactions that are part of the contract
                        //ex
                            //productDao.insert()
                //loose stubbing cause false positives
                    //prefer explicit when(...).thenReturn(...)for the calls you expect
                //unnecessary stubbing
                    //if a stubbed call is never  used by SUT, you'll get unneeded  stubbing noise.
                    //Mockito can be strict
                    //remove unused stubs
                //mixing integration and mock tests
                    //keep them separate
                    //do NOT use  @MockBean in tests meant to validate DB interactions
                //assertions that rely  on exact SQL in integration tests
                    //instead assert DB state and business outcomes
        //Mockito
            //Purpose
                //Software failures come from 
                    //uncertainty
                        //Networks
                        //Databases
                        //Time
                        //Concurrency
                        //External Services
                    //Mockito removes said uncertainty
                        //replacing collaborators with deterministic  doubles
            //What it ACTUALLY is
                //does NOT test logic
                    //INSTEAD controls dependencies
                //does NOT test correctness
                    //INSTEAD DB and respective integration tests do
                //is NOT for all tests
                    //INSTEAD for unit isolation
                //does NOT replace integration tests
                    //INSTEAD complements them
                //IS a dynamic proxy framework that intercepts method calls at runtime and allows you to 
                    //define deterministic responses and verify interactions
            //Internals(under the hood )
                //Creates runtime proxies(ByteBuddy)
                //Intercepts method calls
                //Record invocations
                //Record stubbed values
                //Enables post-execution verification
                //UNABLE
                    //mock constructors
                //should NOT 
                    //mock value objects
                //CAN 
                    //mock interfaces easily
            //Core Mockito Primitives
                //@Mock
                    //Create Proxy
                //@InjectMocks
                    //Wire Subject
                //when().thenReturn()
                    //Stub behavior
                //verify()
                    //assert interaction
                //ArgumentCaptor
                    //inspect calls
                //@MockBean
                    //Spring-aware mock
                //MockitoExtension
                    //JUnit 5 bridge
            //atomic operation that Mockito enables
                //intercept a method call and decide what happens instead
                //EVERYTHING in Mockito exists for 4 phases + every primitive maps to exactly 1 of these phases:
                    //1) Proxy creation
                        //@Mock creates a runtime-generated proxy object(fake)
                            //implements the same type (interface or class)
                            //intercepts EVERY method call
                            //has 0 business domain logic
                            //intercepts every method call
                            //using libraries such as
                                //ByteBuddy
                                //CGLIB
                                //Java Instrumentation
                        //What happens internally
                            //ByteBuddy generates a subclass or interface proxy
                            //Method calls route to a MockHandler
                            //Calls are recorded with:
                                //Method signature
                                //Arguments
                                //Invocation order
                                //Thread
                            //MockitoExtension-> 
                                //Mockito.mock(interface or class) (DaoProductInterface)->
                                    //ByteBuddy creates proxy  of subclass or interface proxy-> 
                                        //Mock object now points to proxy object
                        //Mockito.Mock()
                            //manual version of @Mock  
                        //@Spy
                        //@InjectMocks
                          //Create an instance of said class (ProductService)
                              //find fields needing mocks
                                 //inject mocks via  constructor/setter/field
                                   //injection order important
                                      //constructor(preferred)-> setter-> field injection(last resort)
                          //part of dependency wiring
                            //if canNOT satisfy dependencies
                              //will create null
                              //or throw initialization errors

                        //Important
                            //Unstubbed methods return:
                                //null(objects)
                                //0 (primitives)
                                //false(booleans)
                                //Empty collections(optional behavior)
                        //What NOT to mock
                            //Domain objects
                            //Value objects
                            //DTOs
                            //Collections
                            //Time-independent logic
                    //2) Stubbing behavior
                        //Define deterministic behavior for uncertainty
                        //we tell the mock what it should return when called
                        //withOUT stubbing 
                           //returns default values
                        //when(...).thenReturn(...)
                        //Internal Mechanics
                            //Mockito internally records a method matcher
                                //Associates with a response
                                    //Stores in a stub registry
                                        //On invocation-> matcher lookup-> return response
                        //Mockito internally records:
                            //method signature +arguments
                            //stubbed return value -> stored in stub registry
                        //when(...).thenThrow(...)
                        //when(...).thenAnswer(...)
                        //doReturn/doThrow/doNothing
                    //3) Invocation recording
                        //most misunderstood part of Mockito
                           //whenever a mock method is called
                              //ex: obj_mock_dao_product_interface.findBySkuId("ABC")
                                 //Mockito does NOT directly return a value
                                     //INSTEAD
                                        //Intercepts a call
                                        //Records invocation
                                        //Looks for stub
                                        //Returns stub result
                                        //list data structure with results used later for
                                           //used later for verification
                        //Primitives involved here
                           //They are NOT explicit API calls
                               //They occur automatically whenever the mock is called
                                  //Example
                                     //obj_product_service.getProduct("ABC")
                                       //inside service
                                          //obj_mock_dao_product_interface.findBySkuId("ABC")
                                            //Mockito intercepts and responds
                                               //invocation recording happens automatically
                                                 //no annotation required
                    //4) Post-execution verification
                       //Now the test checks
                         //Did the expected interactions happen
                            //this uses the invocation history earlier
                               //Mockito primitives here
                                //verify(obj_mock_dao_product_interface).findBySkuId("ABC")
                                   //Mockito scans recorded invocation + checks
                                     //method== findBySkuId
                                     //argument = "ABC"
                                     //count>= 1
                                  //Category
                                    //Post-execution verification
                                //verify(times())
                                //verifyNoInteractions()
                               //verifyNoMoreInteractions()

    //1) Proxy Creation
    @Mock
    DaoProductInterface obj_mock_runntime_proxy_dao_product_interface;


    //wires the @Mock
        //via constructor injection
            //because ProductService has a explicit constructor
    @InjectMocks
    ProductService  obj_inject_mock_product_service;                           

    
    @Test
    void createProdWhenSkuIdExists()
    {
        

        //2) Stubbing behavior
        when(obj_mock_runntime_proxy_dao_product_interface.findBySkuId("ABC-123")).thenReturn(Optional.of(new Product("ABC-123", "widget",BigDecimal.valueOf(95))));

        //3) invocation recording
            //JUnit assertions
        assertThrows(DuplicateSkuException.class,()->obj_inject_mock_product_service.createProduct(new Product("ABC-123", "widget",BigDecimal.valueOf(95))), "skuId exists -> Expected DuplicateSkuException");

        //4) Post-execution verification
        verify(obj_mock_runntime_proxy_dao_product_interface,never()).insert(any(Product.class));
        //verify(obj_mock_runntime_proxy_dao_product_interface,times(0)).insert(any(Product.class));
    }
        
}
                                