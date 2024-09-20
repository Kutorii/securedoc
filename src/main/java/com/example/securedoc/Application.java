package com.example.securedoc;

import com.example.securedoc.domain.RequestContext;
import com.example.securedoc.entity.RoleEntity;
import com.example.securedoc.enumeration.Authority;
import com.example.securedoc.repository.RoleRepository;
import com.example.securedoc.utils.MfaUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.awt.image.BufferedImage;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	// Image converter to display a QRCode in browser
	@Bean
	public HttpMessageConverter<BufferedImage> imageConverter() {
		return new BufferedImageHttpMessageConverter();
	}

//	@Bean
//	CommandLineRunner commandLineRunner(RoleRepository roleRepository) {
//		return args -> {
//			RequestContext.setUserId(0L);
//			var userRole = new RoleEntity();
//			userRole.setName(Authority.USER.name());
//			userRole.setAuthorities(Authority.USER);
//			roleRepository.save(userRole);
//
//			var adminRole = new RoleEntity();
//			adminRole.setName(Authority.ADMIN.name());
//			adminRole.setAuthorities(Authority.ADMIN);
//			roleRepository.save(adminRole);
//
//			RequestContext.run();
//		};
//	}
}
