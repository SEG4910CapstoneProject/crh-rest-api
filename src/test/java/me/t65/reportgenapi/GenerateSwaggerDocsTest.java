package me.t65.reportgenapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Both tests if swagger docs are available and regenerates json This ensures that when testing, the
 * api json is always regenerated by the devs
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@AutoConfigureMockMvc
public class GenerateSwaggerDocsTest {
    private static final String SWAGGER_FILE = "CyberReportHub-api.json";

    @Autowired private TestRestTemplate restTemplate;

    @Test
    public void getSwaggerDocs() throws Exception {
        String swagger = this.restTemplate.getForObject("/v3/api-docs", String.class);
        writeSwaggerFile(swagger);
    }

    private void writeSwaggerFile(String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(SWAGGER_FILE));
        writer.write(content);
        writer.close();
    }
}
