import com.example.echoapi.EchoAPIApplication;
import com.example.loadbalancer.LoadBalancerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(classes = LoadBalancerApplication.class)
@AutoConfigureMockMvc
public class E2ELoadBalancerIT {

    private static final String PATH = "/api/roundrobin";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRoundRobinEndpoint_Success() throws Exception {
        // Start the additional Spring Boot application programmatically
        final ConfigurableApplicationContext server1 = new SpringApplicationBuilder()
                .sources(EchoAPIApplication.class)
                .properties("server.port=8080") // Set a random port for the additional application
                .run();
        final ConfigurableApplicationContext server2 = new SpringApplicationBuilder()
                .sources(EchoAPIApplication.class)
                .properties("server.port=8081") // Set a random port for the additional application
                .run();

        final String requestBody = "{\"key\": \"value\"}";

        mockMvc.perform(MockMvcRequestBuilders.post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(requestBody));

        server1.close();
        server2.close();
    }
}
