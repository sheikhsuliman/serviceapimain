package com.siryus.swisscon.api.config;

import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.naturalprogrammer.spring.lemon.security.LemonJpaSecurityConfig;
import com.naturalprogrammer.spring.lemon.security.LemonUserDetailsService;
import com.siryus.swisscon.api.auth.LemonService;
import com.siryus.swisscon.api.contract.repos.ContractRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleService;
import com.siryus.swisscon.api.contract.repos.ContractCommentRepository;
import com.siryus.swisscon.api.contract.repos.ContractTaskRepository;
import com.siryus.swisscon.api.filter.LemonCommonsCookieAuthFilter;
import com.siryus.swisscon.api.general.reference.ReferenceService;
import com.siryus.swisscon.api.location.location.LocationRepository;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import com.siryus.swisscon.api.tasks.repos.CommentRepository;
import com.siryus.swisscon.api.tasks.repos.MainTaskRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskCheckListRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;
import com.siryus.swisscon.api.tasks.repos.TaskLinkRepository;
import com.siryus.swisscon.api.util.CookieService;
import com.siryus.swisscon.api.util.entitytree.EntityTreeService;
import com.siryus.swisscon.security.DefaultContextResolver;
import com.siryus.swisscon.security.SiryusPermissionChecker;
import com.siryus.swisscon.security.TargetResolverCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.CorsFilter;

@Slf4j
@Component
public class SecurityConfig  extends LemonJpaSecurityConfig {
	private final LemonService lemonService;

	private final LemonUserDetailsService<?, ?> userDetailsService;

	private final CookieService cookieService;

	private final CustomCorsFilter corsFilter;

	private final ProjectUserRoleRepository projectUserRoleRepository;

	private final LocationRepository locationRepository;
	private final MainTaskRepository taskRepository;
	private final SubTaskRepository subTaskRepository;
	private final EntityTreeService treeService;
	private final SubTaskCheckListRepository checkListRepository;
	private final TaskLinkRepository taskLinkRepository;
	private final ContractRepository contractRepository;
	private final ContractTaskRepository contractTaskRepository;
    private final ContractCommentRepository contractCommentRepository;
	private final CompanyUserRoleService companyUserRoleService;
	private final CustomAuthenticationFailureHandler authenticationFailureHandler;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final ReferenceService referenceService;
    private final CommentRepository commentRepository;
        
	@Autowired
	public SecurityConfig(
			LemonService lemonService,
			LemonUserDetailsService<?, ?> userDetailsService,
			CookieService cookieService,
			CustomCorsFilter corsFilter,
			ProjectUserRoleRepository projectUserRoleRepository,
			LocationRepository locationRepository,
			MainTaskRepository taskRepository,
			SubTaskRepository subTaskRepository,
			EntityTreeService treeService,
			SubTaskCheckListRepository checkListRepository,
			TaskLinkRepository taskLinkRepository,
			ContractRepository contractRepository,
			ContractTaskRepository contractTaskRepository,
			ContractCommentRepository contractCommentRepository,
			CompanyUserRoleService companyUserRoleService,
			CustomAuthenticationFailureHandler authenticationFailureHandler,
			CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
			ReferenceService referenceService,
			CommentRepository commentRepository
	) {
		this.lemonService = lemonService;
		this.userDetailsService = userDetailsService;
		this.cookieService = cookieService;
		this.corsFilter = corsFilter;
		this.projectUserRoleRepository = projectUserRoleRepository;
		this.locationRepository = locationRepository;
		this.taskRepository = taskRepository;
		this.subTaskRepository = subTaskRepository;
		this.treeService = treeService;
		this.checkListRepository = checkListRepository;
		this.taskLinkRepository = taskLinkRepository;
		this.contractRepository = contractRepository;
		this.contractTaskRepository = contractTaskRepository;
                this.contractCommentRepository = contractCommentRepository;
		this.companyUserRoleService = companyUserRoleService;
		this.authenticationFailureHandler = authenticationFailureHandler;
		this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
                this.referenceService = referenceService;
		this.commentRepository = commentRepository;
	}


	@Bean
	public PermissionEvaluator createPermissionEvaluator() {
		return new SiryusPermissionChecker(
				new TargetResolverCache(
                                    locationRepository,
                                    taskRepository,
                                    subTaskRepository,
                                    treeService,
                                    checkListRepository,
                                    taskLinkRepository,
                                    contractRepository,
                                    contractTaskRepository, 
                                    contractCommentRepository,
                                    referenceService,
                                    commentRepository
                ),
				new DefaultContextResolver(
                                    this.projectUserRoleRepository,
                                    companyUserRoleService)
		);
	}

	/**
	 * All Endpoints which are accessible without authorization
	 */
	@Override
	protected void authorizeRequests(HttpSecurity http) throws Exception {
		http.headers().frameOptions().disable();
		http.authorizeRequests()
				.mvcMatchers(HttpMethod.GET, "/soa/**").permitAll()
				.mvcMatchers(HttpMethod.GET, "/q/{\\d+}/").permitAll()
				.mvcMatchers(HttpMethod.POST, "/api/rest/auth/forgot-password").permitAll()
				.mvcMatchers(HttpMethod.POST, "/api/rest/auth/reset-password").permitAll()
				.mvcMatchers(HttpMethod.POST, "/api/rest/auth/login").permitAll()
				.mvcMatchers(HttpMethod.POST, "/api/rest/auth/signup").permitAll() //TODO remove after SI-177
				.mvcMatchers(HttpMethod.POST, "/api/rest/auth/signup-from-invite").permitAll()
				.mvcMatchers(HttpMethod.GET, "/api/rest/auth/init").permitAll()
				.mvcMatchers(HttpMethod.POST, "/api/rest/auth/verify-signup-code").permitAll()  //TODO remove after SI-177
				.mvcMatchers(HttpMethod.POST, "/api/rest/auth/verify-signup").permitAll()
				.mvcMatchers(HttpMethod.POST, "/api/rest/auth/sms/send-forgot-password").permitAll()
				.antMatchers(HttpMethod.POST, "/api/rest/auth/users/{\\d+}/verification**").permitAll()
				.antMatchers(HttpMethod.POST, "/api/rest/auth/users/{\\d+}/email**").permitAll()
				.antMatchers(HttpMethod.POST, "/api/rest/auth/users/{\\d+}/resend-verification-mail**").authenticated()
				.antMatchers(HttpMethod.POST, "/api/rest/auth/users/{\\d+}/email-change-request**").authenticated()
				.antMatchers(HttpMethod.POST, "/api/rest/auth/users/{\\d+}/password**").authenticated()
				.mvcMatchers("/api/rest/**").access(isVerifiedAndAuthenticated())
				.mvcMatchers("/**").permitAll();
		super.authorizeRequests(http);
	}

	private String isVerifiedAndAuthenticated() {
		return "not( hasRole('" + UserUtils.Role.UNVERIFIED + "') ) and isAuthenticated()";
	}

	/**
	 * Change login URL
	 */
	@Override
	protected String loginPage() {
		return "/api/rest/auth/login";
	}


	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http.addFilterBefore(corsFilter, CorsFilter.class));
		http.formLogin().failureHandler(authenticationFailureHandler);
	}

	@Override
	protected void tokenAuthentication(HttpSecurity http) {
		http.addFilterBefore(new LemonCommonsCookieAuthFilter(lemonService, blueTokenService, userDetailsService, cookieService),
				UsernamePasswordAuthenticationFilter.class);
	}

	@Override
	protected void exceptionHandling(HttpSecurity http) throws Exception {
		http.exceptionHandling()
				.authenticationEntryPoint(customAuthenticationEntryPoint);
	}
}
