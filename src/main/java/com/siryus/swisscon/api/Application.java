package com.siryus.swisscon.api;

import com.naturalprogrammer.spring.lemon.commonsjpa.LemonCommonsJpaAutoConfiguration;
import com.naturalprogrammer.spring.lemon.commonsweb.LemonCommonsWebAutoConfiguration;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Slf4j
// Remove security and error handling
@SpringBootApplication(exclude = {
		LemonCommonsJpaAutoConfiguration.class,
		//SecurityAutoConfiguration.class,
		ErrorMvcAutoConfiguration.class})
//@ComponentScan({
//		Application.PACKAGE_NAME,
//		"com.naturalprogrammer.spring.lemon"})
// Enable transactions and auditing
@EnableTransactionManagement
@EnableScheduling //necessary to execute methods with @schedule annotations
@EnableSchedulerLock(defaultLockAtMostFor = "30s") //used to execute @scheduled task only once on multiple instances
@ComponentScan({Application.SOA_PACKAGE_NAME, Application.PACKAGE_NAME})
@EntityScan({Application.PACKAGE_NAME})
@EnableJpaRepositories(
		includeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
						AbstractUserRepository.class, JpaRepository.class})
		}
)
@ImportAutoConfiguration(LemonCommonsWebAutoConfiguration.class)
public class Application {

	public static final String PACKAGE_NAME = "com.siryus.swisscon.api";
	public static final String SOA_PACKAGE_NAME = "com.siryus.swisscon.soa";

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

