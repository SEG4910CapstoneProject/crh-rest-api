package me.t65.reportgenapi;

import me.t65.reportgenapi.db.postgres.repository.*;
import me.t65.reportgenapi.db.services.DbArticlesService;
import me.t65.reportgenapi.db.services.DbUserTagsService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
class ReportGenerationRestApiApplicationTests {
    @MockBean ArticleTypeRepository articleTypeRepository;
    @MockBean MonthlyArticlesRepository monthlyArticlesRepository;
    @MockBean UserRepository userRepository;
    @MockBean BCryptPasswordEncoder passwordEncoder;
    @MockBean UserFavouriteRepository userFavouriteRepository;
    @MockBean UserTagRepository userTagRepository;
    @MockBean UserTagArticleRepository userTagArticleRepository;

    @MockBean DbUserTagsService dbUserTagsService;
    @MockBean DbArticlesService dbArticlesService;

    @Test
    void contextLoads() {}
}
