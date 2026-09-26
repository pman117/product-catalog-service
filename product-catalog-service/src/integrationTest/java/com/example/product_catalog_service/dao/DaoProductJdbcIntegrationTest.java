package com.example.product_catalog_service.dao;

import com.example.product_catalog_service.domainobjectmodel.Product;
import com.example.product_catalog_service.dao.intrface.DaoProductInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;

//why Testcontainers
    //problems we face whenm testing source code  that talks to external systems(DB, message bus, S3):
        //local environmental drift
            //dev machines + CI differ from production(engine version , config)
        //mock insufficiency
            //unit mocks avoid external dependencies but canNOT catch SQL dialect,schema, or behavioral differences
        //flaky integration
            //shared test environments cause order/dependency problems and are not isolated by run
        //hard to reproduce bugs
    //Goal
        //tests are reproducible, deterministic, and authoritative(they run against real services that behave like production) but safe to run in CI and local dev
            //solution
                //programmatically created isolated, short lived Docker containers that run the real service and are managed by the test framework
                    //Testcontainers
    //Testcontainer what is it
        //a library (Java first, has modules for other languages) that starts Docker containers from tests
        //provides modules for common services
            //MySQLContainer
            //PostgreSQLContainer
            //KafkaContainer
            //RabbitMQContainer
            //LocalStackContainer
            //GenericContainer ...etc..
        //handles lifecycle start stop containers
            //2 ways
                //per class 
                //per test
        //Offers wait strategies
            //wait until port open
            //log message seen
            //HTTP  endpoint returns success
            //custom checks
        //intergrates with frameworks and provides convenience methods to wire container properties into app's config
            //JUnit4/5
            //Spock
            //Spring
        //supports programmatic copy files into container, mounting volumes,network handling,and Docker Compose-like orchestration(via the Compose module or network features)
    //Docker vs. Testcontainers
        //Similarities
            //Both docker images + containers
            //Both run the same  service images(mysql,postgres, kafka, redis)
            //Both can be run locally and in CI environments
        //Differences/Tradeoffs
            //Control and Scope
                //Docker
                    //run containers manually  or  docker-compose to define stack
                        //Relatively good for local dev and complex stacks
                    //pros
                        //full control
                        //good for 
                            //repeatable local orchestration
                            //explicit configuration files(docker-compose.yml)
                    
                    //cons
                        //manual lifecycle
                        //harder to tie containers to individual test runs
                        //shared state between tests if not clean
                //Testcontainers
                    //starts container from tests(programmatic), manages lifecycle tied to last execution
                        //pros
                            //per test/per class isolation
                            //programmatic wiring
                            //tight test reproducibility
                            //automatic cleanup
                        //cons
                            //requires Docker available to the test runner
                            //some extra JVM dependency
                    
                       
            //Integration with test frameworks
                //Docker
                    //is external
                        //must manually start/stop and write connectors to tests
                //Testcontainers
                    //integrates with JUnit and provides @Container and @ Testcontainers for lifecycle and utilities to set system properties for Spring

            //Isolation
                //Docker-compose
                    //often used for sharing stack across developer machines or CI jobs but usually shared across tests, unless orhcestrated per-test

                //Testcontainers
                    //intended to give per test/per class isolation
                        //containers can be started and stopped automatically
            //Determinism
                //Docker
                //Testcontainers
                    //relatively better for deterministic integration tests 
                        //because can enforce startup ordering/wait strategies and run in a fresh container
            //Performance and Resource cost
                //Docker
                    //relatively time cost of starting containers repeatedly vs single long live compose stack
                        //Testcontainers has caching strategies and a "reuse" mode to mitigate this
                        //docker-compose is faster for iterating with multiple services if you dont need per-test isolation
                
            //When to use which
                //Testcontainers
                    //for authorotative integration tests in CI  and per-PR runs, when you want tests to validate behavior against the reals service
                    //CI/test automation
                    //when you want tests to bring up only the services they need and automatically tear them down
                    //when you want per test/per class isolation and programmatic property wiring
                    //you want tests to be authorotative and reproducible in CI
                //docker-compose
                    //for local development when you want a persistent stack(expensive to start for each stack) to iterate fast
                    //dev env
                    //single reproducilble stack for local  development and manual debugging
                    //for heavy intergration/E2E where shipping many containers per test would be slow
                        //can still use Testcontainers' Compose module to control a compose stack
            //Testcontainers
                //has a Docker Compose module(or a DockerComposeContainer)
                    //so tests can start a compose stack if needed
                        //BUT relatively less fined grained than using container classes
            //Testcontainers + Spring Boot how to wire DB containers into Spring
                //relatively MOST common pattern for DAO/integration tests:
                    //create a container(MySQL/Postgres,etc) and JUnit 5 @Container
                    //use @Testcontainers on the test class
                    //use @DynamicPropertySource to register the container-derived DB URL/credentials into Spring's environment for the test
            //Testcontainers JUnit annotations + lifecycle details
                //@Testcontainers
                    //class level JUnit 5 annotation that enables container lifecycle hooks
                    //when placed in a test class
                        //enables Testcontainers support
                            //it orchestrates the lifecycle of containers annotated with @Container
                    //JUnit Jupiter extension that integrates Testcontainers into the JUnit lifecycle
                    //makes the JUnit extension aware of container rules and registers shutdown hooks(Ryuk) to ensure cleanup of resources
                //@Container
                    //marks container field to be managed(start/stop) automatically by JUnit extension
                    //static field + @Container -> 
                        //started 1 time per test class(shared by test methods in the class), 
                        //container lifecycle bound to test class, 
                        //use for containers that are expensive to start and can be safely shared across test methods
                            //ensure test methods do not interfere with shared DB state or cleanup state between tests
                    //non static field + @Container->
                        // started before each test method(more isolation but slower)
                            //when tests must be FULLY isolated and not share a DB or when tests run
                        //use when tests must be fully isolated and cannot share a DB or when tests run concurrently
                            //to avoid collision
                //@DynamicPropertySource
                    //must be a static method recieveing
                    //static methods executed by Spring Test before the application context loads
                        //the properties are registered before Spring Boot auto-configuration runs
                            //so the DataSource and Flyway will use these properties
                        //essential when the property values depend on runtime behavior(like JDBC URL of a Testcontainers container)
                        //DyanmicPropertyRegistery
                            //to programmatically register properties into the Environment
                            //void add(String key, Supplier<String> valueSupplier)
                            //void add(String key, Supplier<Number> valueSupplier)
                            //registry.add("property.name",container::getJdbcUrl) or other primitive /wrapper typed suppliers
                                //registers a supplier that is invoked lazily after the container starts
                                    //ensures correct wiring
                            //DynamicPropertyRegistry.add also supports supplying a Supplier<String>
                                //standard/idiomatic wayt to integrate Testcontainers + SpringBoot without manually setting properties
                //GenericContainer,MySQLContainer,PostgresSQLContainer,KafkaContainer,LocalStackContainer
                    //specialized helper classes with convenience methods
                //withInitScript(...)
                //copyFileToContainer(...),withClasspathResourceMapping(...)
                //waitingFor(Wait.forLogMessage(".*ready.*\\n",1)) or waitingFor(Wait.forListeningPort())

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)  // ← ADD THIS so full Spring context is discarded after the class runs and thus canNOT pollute  @WebMvcTest slice
@Testcontainers
//integration test with @SpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DaoProductJdbcIntegrationTest
{
    //Logger object
    private static final Logger objLogger = LoggerFactory.getLogger(DaoProductJdbcIntegrationTest.class);

    @Container
    static final MySQLContainer<?> obj_mysql_container= new MySQLContainer<>("mysql:8.1")
        .withDatabaseName("integrationtest")
        .withUsername("integrationtest")
        .withPassword("integrationtest");

    
    @DynamicPropertySource
   static void propsRegisterJdbcMysql(DynamicPropertyRegistry obj_dynami_prop_registry)
   {
        obj_dynami_prop_registry.add("spring.datasource.url", obj_mysql_container::getJdbcUrl);
        obj_dynami_prop_registry.add("spring.datasource.username", obj_mysql_container::getUsername);
        obj_dynami_prop_registry.add("spring.datasource.password", obj_mysql_container::getPassword);

        //Optionally ensure flyway is on
        obj_dynami_prop_registry.add("spring.flyway.enabled", () -> "true");
        obj_dynami_prop_registry.add("spring.flyway.cleanDisabled", () -> "false");
        obj_dynami_prop_registry.add("spring.flyway.url", obj_mysql_container::getJdbcUrl);
        obj_dynami_prop_registry.add("spring.flyway.user", obj_mysql_container::getUsername);
        obj_dynami_prop_registry.add("spring.flyway.password", obj_mysql_container::getPassword);
        obj_dynami_prop_registry.add("spring.flyway.locations", () -> "classpath:/db/migration");
        obj_dynami_prop_registry.add("spring.flyway.baselineOnMigrate", () -> "true");

        

        //Optionally set Spring profile dynamically if desired
        obj_dynami_prop_registry.add("spring.profiles.active", ()-> "integration");


   }


    //define and declare DaoProductInterface object
   @Autowired
   private  com.example.product_catalog_service.dao.intrface.DaoProductInterface obj_dao_prod_interface;

   @Test
    void debugFlyway() 
    {
        System.out.println(">>> JDBC URL = " + obj_mysql_container.getJdbcUrl());
    }

   
   @Test
   void insertAndFindBySkuId()
   {
        //define + declare Product object
        Product obj_prod=  new Product("ABC-123", "widget",BigDecimal.valueOf(95));

        int row_id_numb= obj_dao_prod_interface.insert(obj_prod);

        //check if  row_id_numb == 1
        assertEquals(1, row_id_numb);

        //check if obj_prod exists
        assertNotNull(obj_dao_prod_interface.findBySkuId("ABC-123"),"Product object MUST exist");

        Optional<Product> objOptionalProdFindBySkuId =obj_dao_prod_interface.findBySkuId("ABC-123");

        //check
            //print
        assertTrue((objOptionalProdFindBySkuId).isPresent());
        //objOptionalProdFindBySkuId.map(Product::getSkuId).ifPresent(System.out::println);

        

        //objLogger.info("DB skuId: {}",objOptionalProdFindBySkuId.get().getSkuId());



        
   }


}
