package com.o1teck;


import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;


@EnableAsync
@SpringBootApplication
@EnableGlobalMethodSecurity(securedEnabled=true, prePostEnabled=true)
public class App extends SpringBootServletInitializer {

	public static void main(String[] args) {
		
		SpringApplication.run(App.class, args);
	}
	
	@Override   //This method allows us to run/deploy to existing Tomcat (rather than embedded)
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application){
		
		return application.sources(App.class);
	}
	
	//The point of this is to be able to pass in a class and configure how the View works thereby
	@Bean //We're telling Spring to consider this a bean
	public UrlBasedViewResolver tilesViewResolver(){
		UrlBasedViewResolver tilesViewResolver = new UrlBasedViewResolver();
	
		tilesViewResolver.setViewClass(TilesView.class);
		
		return tilesViewResolver;
	}
	
	
	@Bean
	public TilesConfigurer tilesConfigurer(){
		
		TilesConfigurer tilesConfigurer = new TilesConfigurer();
		
		//Tell Tiles what configuration to use
		String[] defs = {"/WEB-INF/tiles.xml"};
		tilesConfigurer.setDefinitions(defs);
		
		return tilesConfigurer;
	}
	
	//BCrypt password encoding
	@Bean
	PasswordEncoder getEncoder(){
		return new BCryptPasswordEncoder();
	}
	
	
	@Bean 
	PolicyFactory getUserHtmlPolicy(){
		return new HtmlPolicyBuilder()
				.allowCommonBlockElements()
				.allowCommonInlineFormattingElements()
				.toFactory();  
	}
	
	/*
	@Bean
	public EmbeddedServletContainerCustomizer errorHandler(){
		return new EmbeddedServletContainerCustomizer(){
			
		};
	}
	*/
	
	
	//Custom error handling config
	@Bean
	public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> errorHandler(){
		
			return new WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>() {
				 
				@Override
				public void customize(ConfigurableServletWebServerFactory factory){
					factory.addErrorPages(new ErrorPage(HttpStatus.FORBIDDEN, "/403"));	
			}
		};
	}
	
	
	/*
	@Configuration
	public class ServerConfig {
		@Bean
		public ConfigurableServletWebServerFactory webServerFactory() {
			TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();

			factory.addErrorPages(new ErrorPage(HttpStatus.FORBIDDEN, "/403"));
			return factory;
		}
	}
	*/

}
