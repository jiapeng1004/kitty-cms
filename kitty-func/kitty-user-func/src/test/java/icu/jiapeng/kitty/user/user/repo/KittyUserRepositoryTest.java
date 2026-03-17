//package icu.jiapeng.kitty.user.user.repo;
//
//
//import icu.jiapeng.kitty.user.user.entity.KtUser;
//import icu.jiapeng.kitty.user.user.mapper.KtUserUserMapper;
//import jakarta.annotation.Resource;
//import lombok.SneakyThrows;
//import org.apache.commons.io.FileUtils;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.data.r2dbc.autoconfigure.DataR2dbcAutoConfiguration;
//import org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration;
//import org.springframework.boot.r2dbc.autoconfigure.R2dbcInitializationAutoConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
//import org.springframework.r2dbc.core.DatabaseClient;
//import org.springframework.test.context.ActiveProfiles;
//import reactor.test.StepVerifier;
//
//import java.time.Instant;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = KittyUserRepositoryTest.class)
//@ActiveProfiles("loc-junit") // 设置使用 application-loc-junit 配置文件
////@Sql(scripts = "/sql/reboot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD) // 每个单元测试结束后，清理 DB
////@Sql(scripts = "/sql/kittycms_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) // 每个单元测试结束后，清理 DB
//@Import({
//        // DB 配置类
//        R2dbcAutoConfiguration.class,
//        R2dbcInitializationAutoConfiguration.class,
//        DataR2dbcAutoConfiguration.class,
//})
//@EnableR2dbcRepositories(basePackages = "icu.jiapeng.kittycms.user") // 启用R2DBC仓库扫描
//public class KittyUserRepositoryTest/* extends BaseDbUnitTest*/ {
//
//    @Resource
//    private KtUserUserMapper kittyUserRepository;
//    @Resource
//    private DatabaseClient databaseClient;
//
//    @SneakyThrows
//    @BeforeEach
//    public void setUp() {
//        ClassPathResource pathResourceClean = new ClassPathResource("/sql/clean.sql");
//        DatabaseClient.GenericExecuteSpec sqlClean = databaseClient.sql(FileUtils.readFileToString(pathResourceClean.getFile(), "utf-8"));
//        sqlClean.then().block();
//        //获取当前java启动的工作目录
//        ClassPathResource pathResource = new ClassPathResource("/sql/kittycms_user.sql");
//        DatabaseClient.GenericExecuteSpec sql = databaseClient.sql(FileUtils.readFileToString(pathResource.getFile(), "utf-8"));
//        sql.then().block();
//    }
//
//    @Test
//    public void caseUserInsert() {
//        KtUser kittyUser = new KtUser();
//        kittyUser.setNew(true);
//        kittyUser.setId("1");
//        kittyUser.setLoginName("admin");
//        kittyUser.setRealName("管理员");
//        kittyUser.setNickName("管理员");
//        kittyUser.setPwd("<PASSWORD>");
//        kittyUser.setPhone("12345678901");
//        kittyUser.setEmail("<EMAIL>");
//        kittyUser.setIdCard("123456789012345678");
//        kittyUser.setStatus(1);
//        kittyUser.setCreateTime(Instant.now());
//        kittyUser.setUpdateTime(Instant.now());
//
//        StepVerifier.create(kittyUserRepository.save(kittyUser))
//                .expectNextCount(1)
//                .verifyComplete();
//
//        StepVerifier.create(kittyUserRepository.findById("1"))
//                .expectNextMatches(user -> "admin".equals(user.getLoginName()))
//                .verifyComplete();
//    }
//
//}