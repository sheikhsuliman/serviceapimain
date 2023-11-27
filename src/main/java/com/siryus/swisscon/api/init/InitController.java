/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.init;

import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.AuthException;
import com.siryus.swisscon.api.auth.LemonService;
import com.siryus.swisscon.api.auth.permission.PermissionRepository;
import com.siryus.swisscon.api.auth.user.DashboardUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserCategory;
import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.base.SimpleEntityDTO;
import com.siryus.swisscon.api.catalog.GlobalCatalogReader;
import com.siryus.swisscon.api.company.companylegaltype.CompanyLegalTypeService;
import com.siryus.swisscon.api.company.numworkersofcompany.NumWorkersOfCompanyService;
import com.siryus.swisscon.api.customroles.CustomRoleReader;
import com.siryus.swisscon.api.general.country.CountryService;
import com.siryus.swisscon.api.general.currency.CurrencyDTO;
import com.siryus.swisscon.api.general.currency.CurrencyService;
import com.siryus.swisscon.api.general.gender.GenderService;
import com.siryus.swisscon.api.general.langcode.LangCode;
import com.siryus.swisscon.api.general.langcode.LangCodeService;
import com.siryus.swisscon.api.general.title.TitleService;
import com.siryus.swisscon.api.general.unit.UnitDTO;
import com.siryus.swisscon.api.general.unit.UnitRepository;
import com.siryus.swisscon.api.project.projecttype.ProjectTypeService;
import com.siryus.swisscon.soa.ApiConfiguration;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author hng
 */
@RestController("initController")
@Api(
    tags = "Init",
    description = "Retrieve initial dataset a user sees after log in"
)
public class InitController {
    private static final Logger FE_LOGGER = LoggerFactory.getLogger("com.siryus.swisscon.fe-logger");
    private static final String FE_LOGGER_MARKER = "[FE-LOG] - ";

    private static final Logger LOGGER = LoggerFactory.getLogger(InitController.class);

    @Autowired
    private LemonService lemonService;

    @Autowired
    private CompanyLegalTypeService companyLegalTypeService;

    @Autowired
    private GlobalCatalogReader catalogReader;

    @Autowired
    private UserService userService;

    @Autowired
    private NumWorkersOfCompanyService numWorkersService;

    @Autowired
    private GenderService genderService;

    @Autowired
    private TitleService titleService;

    @Autowired
    private CustomRoleReader rolesReader;

    @Autowired
    private CountryService countryService;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private LangCodeService langCodeService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ProjectTypeService projectTypeService;

    @Autowired
    private UnitRepository unitRepository;


    /**
     * This endpoint should only be made available to authenticated in users.
     * It is called exclusively from pages living under the dashboard frontend domain.
     * 
     * @return DashboardInitDTO 
     */
    @GetMapping("/api/rest/dashboard/init")
    @ApiOperation(value = "Get dashboard initialization data", notes = "Gets information associated to the currently logged in user and global settings data") 
    public InitDTO getDashboardInitData() {
        UserDto crtUser = LecwUtils.currentUser();
        if (crtUser == null) {
            throw AuthException.unauthorizedRequest("Not Authenticated");
        }       
        
        return getInitData();
    }
    
    /**
     * This endpoint is made available to both authenticated in and non-authenticated users.
     * It is called from pages living under the auth frontend domain (e.g. Sign Up and Login) but
     * also as a way to check whether the user is logged in.
     * 
     * @return 
     */
    @GetMapping("/api/rest/auth/init")
    @ApiOperation(value = "Get pre-authentication initialization data", notes = "Contains user data if the user is logged in (otherwise null) and global settings data")
    public InitDTO getInitData() {
        //TODO add bank account to init data

        UserDto crtUser = LecwUtils.currentUser(); 
    
        User u = crtUser != null ? userService.findById(Integer.parseInt(crtUser.getId())) : null;

        DashboardUserDTO user = addRabbitJwt(DashboardUserDTO.from(u));

        List<SimpleEntityDTO> countries = countryService.getCountries().stream().map(c -> SimpleEntityDTO
                .builder()
                .id(c.getId())
                .name(c.getCode())
                .build()).collect(Collectors.toList());

        List<String> languages = langCodeService.findAll().stream().map(LangCode::getId).collect(Collectors.toList());

        List<CurrencyDTO> currencies = currencyService.findAll().stream().map(CurrencyDTO::from).collect(Collectors.toList());

        InitSettingsDTO settings = new InitSettingsDTO(
                companyLegalTypeService.findAll(),
                Arrays.asList(UserCategory.values()),
                catalogReader.listRoots(),
                numWorkersService.findAll(),
                genderService.findAll(),
                titleService.findAll(),
                rolesReader.listRoles(),
                projectTypeService.findAll(),
                countries,
                rolesReader.listPermissions(),
                languages,
                currencies,
                getUnits());

    
        return new InitDTO(user, settings, buildFeaturesMap());
    }

    private DashboardUserDTO addRabbitJwt(DashboardUserDTO from) {
        return from == null ? null : from.toBuilder().rabbitJwt(lemonService.createRabbitJwt(from.getEmail(), from.getId())).build();
    }

    private Map<String, String> buildFeaturesMap() {
        return ApiConfiguration.buildVarsMap(ApiConfiguration.Var.MEDIA_WIDGET_V2);
    }

    @PostMapping("/api/rest/fe-log")
    public void feLog(@RequestBody FELogRequest logRequest) {
        var message = FE_LOGGER_MARKER + logRequest.getMessage();
        if("debug".equals(logRequest.getLevel())) {
            FE_LOGGER.debug(message);
        }
        else if("info".equals(logRequest.getLevel())) {
            FE_LOGGER.info(message);
        }
        else if("warn".equals(logRequest.getLevel())) {
            FE_LOGGER.warn(message);
        }
        else if("error".equals(logRequest.getLevel())) {
            FE_LOGGER.error(message);
        }
        else {
            FE_LOGGER.info(message);
        }
    }

    private List<UnitDTO> getUnits() {
        return unitRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream().map(UnitDTO::from).collect(Collectors.toList());
    }
}
