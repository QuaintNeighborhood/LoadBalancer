package e2e;

import com.example.echo.EchoApplication;
import com.example.loadbalancer.LoadBalancerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest(classes = LoadBalancerApplication.class)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-integrationtest.properties")
public class E2ELoadBalancerIT {

    private static final String PATH = "/api/v1/loadbalancer";
    private static final String REQ_BODY = "{\"key\": \"value\"}";
    private static final String SERVER_ONE_PORT_PROPS = "server.port=8080";
    private static final String SERVER_TWO_PORT_PROPS = "server.port=8081";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRoundRobinEndpoint_BadRequest_InvalidJSON() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\": }"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testRoundRobinEndpoint_UnsupportedMediaType() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(PATH))
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());

        mockMvc.perform(MockMvcRequestBuilders.post(PATH)
                        .contentType(MediaType.APPLICATION_XML))
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());

        mockMvc.perform(MockMvcRequestBuilders.post(PATH)
                        .content(""))
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());
    }

    @Test
    public void testRoundRobinEndpoint_AllServersHealthy() throws Exception {
        final ConfigurableApplicationContext server1 = new SpringApplicationBuilder()
                .sources(EchoApplication.class)
                .properties(SERVER_ONE_PORT_PROPS)
                .run();
        final ConfigurableApplicationContext server2 = new SpringApplicationBuilder()
                .sources(EchoApplication.class)
                .properties(SERVER_TWO_PORT_PROPS)
                .run();

        mockMvc.perform(MockMvcRequestBuilders.post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQ_BODY))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(REQ_BODY));

        server1.close();
        server2.close();
    }

    @Test
    public void testRoundRobinEndpoint_AllServersFail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQ_BODY))
                .andExpect(MockMvcResultMatchers.status().isServiceUnavailable())
                .andExpect(res -> assertInstanceOf(ResponseStatusException.class, res.getResolvedException()));
    }

    @Test
    public void testRoundRobinEndpoint_OneServerFail() throws Exception {
        final ConfigurableApplicationContext server2 = new SpringApplicationBuilder()
                .sources(EchoApplication.class)
                .properties(SERVER_TWO_PORT_PROPS)
                .run();

        mockMvc.perform(MockMvcRequestBuilders.post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQ_BODY))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(REQ_BODY));

        server2.close();
    }
}
