package com.viettel.deliverymanagement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class DeliveryManagementApplicationTests {

    @Autowired
    private WebApplicationContext applicationContext;

    @Test
    void contextLoads() {
    }

    @Test
    void openApiDocumentIsGenerated() throws Exception {
        webAppContextSetup(applicationContext).build()
                .perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", startsWith("3.")));
    }

}
