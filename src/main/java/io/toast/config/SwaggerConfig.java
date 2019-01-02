package io.toast.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

    /**
     * Docket: A builder which is intended to be the primary interface into the
     * Springfox framework.
     * <p>
     * Provides sensible defaults and convenience methods for configuration.
     */
    @Bean
    public Docket api() {

        DocumentationType documentationType = DocumentationType.SWAGGER_2;

        Docket d = new Docket(documentationType);

        /*
            select() method returns an instance of ApiSelectorBuilder,
            which provides a way to control the endpoints exposed by Swagger.
         */
        return d.select()
                .apis(basePackage("io.toast"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {

        String applicationName = "Toast";
        String applicationUrl = "https://toast.io";

        String description = "토익스피킹 타이머 TOAST";
        String version = "1.0.0";
        String termsOfServiceUrl = applicationUrl + "/terms";
        Contact contact = new Contact(applicationName, applicationUrl, "service@toast.io");
        String license = "Apache License 2.0";
        String licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0";

        return new ApiInfo(
                applicationName,
                description,
                version,
                termsOfServiceUrl,
                contact,
                license,
                licenseUrl,
                new ArrayList<>()
        );
    }

}
