package com.iss.hanson.hanson.configuration;

import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

import static com.iss.hanson.hanson.common.constant.Constant.*;

/**
 * @author Hanson
 * @date 2021/11/19  15:56
 */
@Configuration
@EnableSwagger2
@EnableSwaggerBootstrapUI
public class SwaggerConfiguration {

	@Value("${spring.application.name:}")
	private String appName;

	@Bean
	public Docket createRestApi(){
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
													  .pathMapping("/")
													  .select()
													  .apis(RequestHandlerSelectors.any())
													  .paths(Predicates.not(PathSelectors.regex("/error.*")))
													  .paths(PathSelectors.regex("/.*"))
													  .build()
													  .globalOperationParameters(defaultHeader());
	}

	private ApiInfo apiInfo(){
		return new ApiInfoBuilder()
			.title(appName)
			.description(String.format("This is a restful api document of %s.",appName))
			.version("1.0")
			.build();
	}

	private static List<Parameter> defaultHeader(){
		ParameterBuilder token = new ParameterBuilder();
		token.name(TOKEN).description("用户令牌").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
		//签名
		ParameterBuilder sign = new ParameterBuilder();
		sign.name(SIGNATURE).description("签名").modelRef(new ModelRef("string")).parameterType("header").required(true).build();
		ParameterBuilder timestamp = new ParameterBuilder();
		timestamp.name(SIGNATURE_TIMESTAMP).description("签名-时间").modelRef(new ModelRef("string")).parameterType("header").required(true).build();
		ParameterBuilder appid = new ParameterBuilder();
		appid.name(SIGNATURE_APP_ID).description("签名-appId").modelRef(new ModelRef("string")).parameterType("header").required(true).build();
		ParameterBuilder lang = new ParameterBuilder();
		appid.name(LANG).description("语言").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
		List<Parameter> pars = new ArrayList<>();
		pars.add(token.build());
		pars.add(sign.build());
		pars.add(timestamp.build());
		pars.add(appid.build());
		return pars;
	}
}
