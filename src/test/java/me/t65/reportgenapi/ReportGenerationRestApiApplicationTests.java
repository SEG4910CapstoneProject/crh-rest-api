package me.t65.reportgenapi;

import me.t65.reportgenapi.db.postgres.repository.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
class ReportGenerationRestApiApplicationTests {
    @MockBean ArticleTypeRepository articleTypeRepository;
    @MockBean MonthlyArticlesRepository monthlyArticlesRepository;

    @Test
    void contextLoads() {}
}
