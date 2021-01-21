package com.hines.playerscraper.swagger;

import com.fasterxml.classmate.TypeResolver;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static java.util.Arrays.asList;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

/**
 * Defines Swagger
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration implements WebMvcConfigurer
{


    @Override
    public void addViewControllers(ViewControllerRegistry registry)
    {
        registry.addRedirectViewController("/", "/swagger-ui.html").setContextRelative(true);
    }

    @Autowired
    private TypeResolver typeResolver;

    @Bean
    public Docket productApi()
    {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfo())
            .pathMapping("/")
            .directModelSubstitute(LocalDate.class, String.class)
            .genericModelSubstitutes(Optional.class)
            .ignoredParameterTypes(ApiIgnore.class);
    }

    private ApiInfo apiInfo()
    {
        return new ApiInfoBuilder()
            .title("ESPN Player API - The League")
            .contact(new Contact("Sir Hiss", "", ""))
            .version("1.0")
            .termsOfServiceUrl("Tier 1")
            .build();
    }
}
