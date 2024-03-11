package com.example.echo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class EchoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	private static final String PATH = "/api/v1/echo";

	@Test
	public void testEchoEndpoint_Success() throws Exception {
		final String requestBody = "{\"key\": \"value\"}";

		mockMvc.perform(MockMvcRequestBuilders.post(PATH)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(requestBody));
	}

	@Test
	public void testEchoEndpoint_BadRequest() throws Exception {
		final String requestBody = "{\"key\": }";

		mockMvc.perform(MockMvcRequestBuilders.post(PATH)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	public void testEchoEndpoint_UnsupportedMediaType() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(PATH))
				.andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());

		mockMvc.perform(MockMvcRequestBuilders.post(PATH)
						.contentType(MediaType.APPLICATION_XML))
				.andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());

		mockMvc.perform(MockMvcRequestBuilders.post(PATH)
						.content(""))
				.andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());
	}
}
