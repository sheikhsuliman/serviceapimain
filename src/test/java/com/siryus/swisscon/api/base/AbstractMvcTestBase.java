package com.siryus.swisscon.api.base;

import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.SdkHttpMetadata;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.naturalprogrammer.spring.lemon.commons.security.GreenTokenService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.siryus.swisscon.api.Application;
import com.siryus.swisscon.api.auth.EmailLinkUtil;
import com.siryus.swisscon.api.auth.LemonRepository;
import com.siryus.swisscon.api.auth.LemonService;
import com.siryus.swisscon.api.auth.permission.PermissionName;
import com.siryus.swisscon.api.auth.permission.PermissionRepository;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.sms.AwsSmsService;
import com.siryus.swisscon.api.auth.sms.ExtendedTokenService;
import com.siryus.swisscon.api.auth.user.SwissConUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.catalog.GlobalCatalogReader;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleService;
import com.siryus.swisscon.api.config.ClockConfig;
import com.siryus.swisscon.api.customroles.CustomRoleReader;
import com.siryus.swisscon.api.file.file.FileS3PersistenceService;
import com.siryus.swisscon.api.file.mock.MockS3Client;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyRepository;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleService;
import com.siryus.swisscon.api.util.CookieService;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.parsing.Parser;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(TestResultLoggerExtension.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:application-test.properties")
@FlywayDataSource()
public abstract class AbstractMvcTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMvcTestBase.class);

    private static final String MIME_APPLICATION_JSON_UTF8 = "application/json; charset=UTF-8";
    private static final RequestSpecification SPEC_JSON = (new RequestSpecBuilder()).setContentType("application/json; charset=UTF-8").setAccept("application/json; charset=UTF-8").build();

    protected static final Integer ADMIN_ID = 1;
    protected static final String ADMIN_EMAIL = "test@siryus.com";
    protected static final String ADMIN_PASSWORD = "test";

    protected static final String SET_COOKIE_HEADER = "Set-Cookie";

    protected static final String ORIGIN = "https://www.siryus.com";

    protected static final String BASE_PATH = "/api/rest";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Map<Integer, Cookie> cookies = new HashMap<>(6);

    protected TestHelper testHelper;

    @Value("${lemon.default-signup-token}")
    protected String defaultSignupToken;

    @Value("${lemon.application-url}")
    protected String applicationUrl;

    @Value("${base.api.url}")
    protected String baseApiUrl;

    @LocalServerPort
    public int port;

    @Autowired
    protected CookieService cookieService;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private Flyway flyway;

    @Autowired
    protected LemonRepository userRepository;

    @Autowired
    protected CompanyUserRoleService companyUserRoleService;

    @Autowired
    protected ProjectUserRoleService projectUserRoleService;

    @Autowired
    protected GlobalCatalogReader catalogReader;

    @Autowired
    protected UserService userService;

    @Autowired
    LemonService lemonService;

    @Autowired
    GreenTokenService greenTokenService;

    @Autowired
    protected ExtendedTokenService extendedTokenService;    
    
    @Autowired
    ProjectCompanyRepository projectCompanyRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    FileS3PersistenceService fileS3PersistenceService;

    @Autowired
    AmazonS3 amazonS3Client;

    @Autowired
    MockS3Client mockS3Client;

    @Autowired
    CustomRoleReader roleReader;

    @Autowired
    protected TestAssert testAssert;

    @Autowired
    protected TestBuilder testBuilder;

    @Autowired
    private AwsSmsService smsService;

    @Mock
    AmazonSNS mockAmazonSns;

    @SpyBean
    protected EmailLinkUtil emailLinkUtil;

    private SwissConUserDTO user;

    protected io.restassured.http.Cookie buildRequestCookie(String header) {
        javax.servlet.http.Cookie c = cookieService.parseCookie(header);

        if (c != null) {
            return buildRequestCookie(c);
        }
        return null;
    }

    private io.restassured.http.Cookie buildRequestCookie(javax.servlet.http.Cookie cookie) {
        return new io.restassured.http.Cookie.Builder(cookie.getName(), cookie.getValue())
                .setVersion(cookie.getVersion())
                .setPath(cookie.getPath())
                .setDomain(cookie.getDomain())
                .setMaxAge(cookie.getMaxAge())
                .setHttpOnly(cookie.isHttpOnly())
                .setSecured(cookie.getSecure())
                .build();
    }

    private String mockLogin(String userName, String password) {

        MvcResult result;
        String json;
        ObjectMapper mapper = new ObjectMapper();
        try {
            result = mvc.perform(post("/api/rest/auth/login")
                    .param("username", userName)
                    .param("password", password)
                    .header("contentType", MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is(HttpStatus.OK.value()))
                    .andReturn();
            json = result.getResponse().getContentAsString();
            this.user = mapper.readValue(json, SwissConUserDTO.class);
        } catch (Exception e) {
            LOGGER.error("Failed logging in", e);
            throw new RuntimeException("Failed logging in", e);
        }
        return result.getResponse().getHeader(SET_COOKIE_HEADER);
    }

    public RequestSpecification loginSpec(String username, String password) {

        RequestSpecification spec = defaultSpec();

        // set cookie required for upcoming requests
        spec.cookie(buildRequestCookie(login(username, password)));

        return spec;
    }

    public RequestSpecification loginSpec() {
        RequestSpecification spec = defaultSpec();

        // set cookie required for upcoming requests
        spec.cookie(buildRequestCookie(cookies.get(ADMIN_ID)));

        return spec;
    }

    public RequestSpecification defaultSpec() {

        RequestSpecBuilder requestSpecBuilder = (new RequestSpecBuilder()).addRequestSpecification(SPEC_JSON).setPort(this.port);
//        if (LOGGER.isDebugEnabled()) {
//            requestSpecBuilder
//                    .addFilter(new RequestLoggingFilter())
//                    .addFilter(new ResponseLoggingFilter());
//        }
        requestSpecBuilder.addFilter(new CaptureErrorFilter());

        return requestSpecBuilder.build();
    }

    @BeforeAll
    public void baseSetUp() {
        testHelper = new TestHelper(this);
        RestAssured.registerParser("text/html", Parser.HTML);

        setupCleanDatabase();
        setupMockLogin();
        setupMockLocalLogin();
        setupRestAssured();
        setupAwsMocks();
    }

    @AfterAll
    public static void doAfterAll() {
        unMockApplicationUser();
    }

    private void setupAwsMocks() {
        PublishResult publishResult = new PublishResult();
        HttpResponse response = new HttpResponse(null, null);
        response.setStatusCode(200);
        publishResult.setSdkHttpMetadata(SdkHttpMetadata.from(response));

        fileS3PersistenceService.setS3Client(mockS3Client);
        smsService.setSnsClient(mockAmazonSns);
        Mockito.when(mockAmazonSns.publish(any())).thenReturn(publishResult);
    }

    private void setupRestAssured() {
        RestAssured.port = this.port;
        RestAssured.urlEncodingEnabled = true;
        // Rest assured should use the JavaTimeModule of the jsr-310 package to implicitly convert ZonedDateTime
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (type, s) -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
                    objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
                    objectMapper.registerModule(new JavaTimeModule());
                    return objectMapper;
                }
        ));
    }

    private void setupCleanDatabase() {
        if (doCleanDatabase()) {
            cleanDatabase();
        }
    }

    private void setupMockLogin() {
        if (doMockLogin()) {
            String cookieStr = mockLogin(ADMIN_EMAIL, ADMIN_PASSWORD);
            cookies.put(ADMIN_ID, cookieService.parseCookie(cookieStr));
        }
    }

    private void setupMockLocalLogin() {
        if (mockLocalLoginUserId() != null) {
            mockApplicationUser(mockLocalLoginUserId());
        }
    }

    @AfterAll
    public void baseShutDown() {
        mockS3Client.cleanMockS3FileFolder();
        fileS3PersistenceService.setS3Client(amazonS3Client);
    }

    protected User setupUser(String email, String password) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(lemonService.encodePassword(password));

        userService.create(u);

        return u;
    }

    protected boolean doMockLogin() {
        return true;
    }

    protected Integer mockLocalLoginUserId() {
        return null;
    }

    protected boolean doCleanDatabase() {
        return true;
    }

    protected Flyway customizeFlyWay(Flyway flyway) {
        // DO NOTHING BY DEFAULT
        return flyway;
    }

    public void cleanDatabase() {
        Flyway customFlyway = customizeFlyWay(flyway);

        customFlyway.clean();
        customFlyway.migrate();
    }


    protected void ensureCookieWorks(Cookie cookie) throws Exception {
        mvc.perform(get("/api/core/context")
                .cookie(cookie))
                .andExpect(status().is(HttpStatus.OK.value()))
        /*.andExpect(jsonPath("$.user.id").value(UNVERIFIED_USER_ID))*/;
    }

    protected String login(String username, String password) {
        // attempt login and test for a proper result
        return given().spec(defaultSpec()).accept(MIME_APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("username", username)
                .param("password", password)
                .when()
                .post("/api/rest/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract().header(SET_COOKIE_HEADER);
    }

    @SuppressWarnings("unchecked")
    public List extractListInPage(JsonPath jsonPath, Class clazz) {
        List<HashMap> content = jsonPath.get("content");
        LinkedList list = new LinkedList<>();
        content.forEach(map -> list.add(OBJECT_MAPPER.convertValue(map, clazz)));
        return list;
    }

    protected <T> List<T> cast(List list, Class<T> classToCastTo) {
        assertNotNull(list);

        return (List<T>) list.stream().map(
                m -> OBJECT_MAPPER.convertValue(m, classToCastTo)
        ).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Transactional
    protected void deleteIfExists(CrudRepository repository, Integer id) {
        if(repository.existsById(id)) {
            repository.deleteById(id);
        }
    }

    public SwissConUserDTO getUser() {
        return user;
    }

    // Uber Testing

    public static String endPoint(String endPointPath) {
        return BASE_PATH + endPointPath;
    }

    public Integer permissionId(PermissionName permissionName) {
        return permissionRepository.getPermissionByName(permissionName.name()).getId();
    }

    public Integer roleId(RoleName roleName) {
        return roleRepository.getRoleByName(roleName.name()).getId();
    }

    public Integer tradeId(String tradeName) {
        return catalogReader.listRoots().stream()
                .filter( n -> n.getName().equals(tradeName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such trade : " + tradeName)).getId();
    }

    public List<Integer> tradeIds(String... tradeNames) {
        Integer[] result = new Integer[tradeNames.length];
        for(var i = 0; i < tradeNames.length; i++ ) {
            result[i] = tradeId(tradeNames[i]);
        }
        return Arrays.asList(result);
    }

    public ExtractableResponse<Response> getResponse(
            RequestSpecification specification, String path, Object body
    ) {
        return getResponse(specification, path, body, HttpStatus.OK);
    }

    public ExtractableResponse<Response> getResponse(
            RequestSpecification specification, String path, Object body, HttpStatus expectedHttpStatus
    ) {
        return given()
                .spec(specification)
                .body(body)
                .post(path)
                .then()
                .assertThat()
                .statusCode(equalTo(expectedHttpStatus.value()))
                .extract();
    }

    public ExtractableResponse<Response> getResponse(RequestSpecification specification, String path, Map<String, ?> params) {
        return getResponse(specification, path, params, HttpStatus.OK);
    }

    public ExtractableResponse<Response> getResponse(
            RequestSpecification specification, String path, Map<String, ?> params, HttpStatus expectedHttpStatus
    ) {
        return given()
                .spec(specification)
                .params(params)
                .contentType("application/x-www-form-urlencoded")
                .post(path)
                .then()
                .assertThat()
                .statusCode(equalTo(expectedHttpStatus.value()))
                .extract();
    }

    public ExtractableResponse<Response> getResponse(RequestSpecification spec, String path) {
        return getResponse(spec, path, HttpStatus.OK);
    }

    public ExtractableResponse<Response> getResponse(RequestSpecification spec, String path, HttpStatus httpStatus) {
        return given()
                .spec(spec)
                .get(path)
                .then()
                .assertThat()
                .statusCode(equalTo(httpStatus.value()))
                .extract();
    }

    class CaptureErrorFilter implements Filter {

        @Override
        public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            Response response = ctx.next(requestSpec,responseSpec);

            if(response.getStatusCode() >= HttpStatus.BAD_REQUEST.value()) {
                LOGGER.warn("REST call {} Failed : {}\n--\n", requestSpec.getURI(), response.asString());
            }

            return response;
        }
    }

    protected void resetClock() {
        ClockConfig.setClock(Clock.systemDefaultZone());
    }
    protected void offsetClock(Duration offset) {
        Clock result = Clock.offset(ClockConfig.clock, offset);
        ClockConfig.setClock(result);
    }

    protected static UserDto mockApplicationUser(Integer userId ) {
        UserDto userDto = new UserDto();
        userDto.setId(userId.toString());
        LemonPrincipal principal = new LemonPrincipal(userDto);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(principal);

        return userDto;
    }

    protected static void unMockApplicationUser() {
        SecurityContextHolder.clearContext();
    }
}

