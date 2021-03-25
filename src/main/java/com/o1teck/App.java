package com.o1teck;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesViewResolver;

import com.cloudinary.Cloudinary;

//  https://github.com/princesoni1989/Spring-Boot-Cloudinary/blob/master/src/main/java/com/cloudinary/upload/UploadApplication.java


@EnableAsync
@SpringBootApplication
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
//@ComponentScan({"com.o1teck.controllers","com.o1teck"})
public class App extends SpringBootServletInitializer implements WebMvcConfigurer {

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	
	@Value("${cloudinary.cloud_name}")
	private String cloudName;

	@Value("${cloudinary.api_key}")
	private String apiKey;

	@Value("${cloudinary.api_secret}")
	private String apiSecret;
	

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
	
	
	
	/*

	@Override // This method allows us to run/deploy to existing Tomcat (rather than embedded)
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(App.class);
	}
	
	*/
	
	@Bean
	public Cloudinary cloudinaryConfig() {
		Cloudinary cloudinary = null;
		cloudinary = new Cloudinary();
		cloudinary.config.cloudName = "nicko1teck";
		cloudinary.config.apiKey = "697697678268857";
		cloudinary.config.apiSecret = "UFH1ZXH_4UZ8XNHchIj8Lwhpszw";
		return cloudinary;
	}
	
	
	/*
	@Bean
	public Cloudinary cloudinaryConfig() {
		Cloudinary cloudinary = null;
		cloudinary = new Cloudinary();
		
		cloudinary.config.cloudName = cloudName;
		cloudinary.config.apiKey = apiKey;
		cloudinary.config.apiSecret = apiSecret;
		
		//this.cloudName = cloudName;
		//this.apiKey = apiKey;
		//this.apiSecret = apiSecret;
		
		return cloudinary;
	}
	*/

	
	/*
	@Bean // We're telling Spring to consider this a bean
	public UrlBasedViewResolver tilesViewResolver() {
		UrlBasedViewResolver tilesViewResolver = new UrlBasedViewResolver();

		tilesViewResolver.setViewClass(org.springframework.web.servlet.view.tiles3.TilesView.class);

		return tilesViewResolver;
	}
	*/
	
	
	@Bean
    public TilesConfigurer tilesConfigurer() {
        TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions(
          new String[] { "tiles.xml" });
        	
        tilesConfigurer.setCheckRefresh(true);
        return tilesConfigurer;
    }
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        TilesViewResolver viewResolver = new TilesViewResolver();
        registry.viewResolver(viewResolver);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
        .addResourceLocations("/css/");
        registry.addResourceHandler("/img/**")
        .addResourceLocations("/img/");
        registry.addResourceHandler("/js/**")
        .addResourceLocations("/js/");
    }
	

	
	

	// BCrypt password encoding
	@Bean
	PasswordEncoder getEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	PolicyFactory getUserHtmlPolicy() {
		return new HtmlPolicyBuilder().allowCommonBlockElements().allowCommonInlineFormattingElements().toFactory();
	}

	/*
	 * @Bean public EmbeddedServletContainerCustomizer errorHandler(){ return new
	 * EmbeddedServletContainerCustomizer(){
	 * 
	 * }; }
	 */

	// Custom error handling config
	@Bean
	public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> errorHandler() {

		return new WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>() {

			@Override
			public void customize(ConfigurableServletWebServerFactory factory) {
				factory.addErrorPages(new ErrorPage(HttpStatus.FORBIDDEN, "/403"));
				//factory.setContextPath("");
			}
			
			
			
		};
	}

	/*
	 * @Configuration public class ServerConfig {
	 * 
	 * @Bean public ConfigurableServletWebServerFactory webServerFactory() {
	 * TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
	 * 
	 * factory.addErrorPages(new ErrorPage(HttpStatus.FORBIDDEN, "/403")); return
	 * factory; } }
	 */

	/////////////////////////////////////////////////////////////////////////////////////////////
	// TRYING TO BUILD IN CLOUDINARY IMP
	/////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {

		// Add Custom Filter
		// FilterRegistration customFilter = servletContext.addFilter("customFilter",
		// new customFilter());
		// customFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class),
		// true, "*.do");
		// customFilter.setInitParameter("appName", "employee");

		super.onStartup(servletContext);
	}

	/*
	 * @Override protected Class<?>[] getRootConfigClasses() {
	 * LOGGER.info("Application initialization started." );
	 * LOGGER.info("WebApplicationInitializer: Configuring Root Config Classes" );
	 * return new Class<?>[] {ServiceConfig.class}; }
	 */

	/*
	 * @Override protected Class<?>[] getServletConfigClasses() { LOGGER.
	 * info("WebApplicationInitializer: Configuring Spring Web Config Classes" );
	 * return new Class<?>[] { SpringWebConfig.class}; }
	 * 
	 * @Override protected String[] getServletMappings() { return new String[] { "/"
	 * }; }
	 * 
	 * @Override protected Class<?>[] getRootConfigClasses() { // TODO
	 * Auto-generated method stub return null; }
	 * 
	 */

	////////////////////////////////////////////////////////////////
	// This was SpringWebConfig in the cloudinary Imp I'm copying
	////////////////////////////////////////////////////////////////

	/*
	@Bean
	public InternalResourceViewResolver jspViewResolver() {
		InternalResourceViewResolver bean = new InternalResourceViewResolver();
		bean.setViewClass(org.springframework.web.servlet.view.JstlView.class);
		bean.setPrefix("/WEB-INF/");
		bean.setSuffix(".jsp");
		return bean;
	}
	 */
}
