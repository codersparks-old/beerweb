package uk.codersparks.hackspace.beerweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.web.SpringServletContainerInitializer;

@SpringBootApplication
public class BeerwebApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(BeerwebApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(applicationClass);
	}

	private static Class<BeerwebApplication> applicationClass = BeerwebApplication.class;

}
