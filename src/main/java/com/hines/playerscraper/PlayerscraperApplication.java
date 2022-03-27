package com.hines.playerscraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import static java.time.Duration.ofMillis;

@EnableAsync
@SpringBootApplication
public class PlayerscraperApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlayerscraperApplication.class, args);
	}


	@Bean(name="espnTemplate")
	public RestTemplate restTemplate(RestTemplateBuilder builder)
	{
		return builder.setConnectTimeout(ofMillis(500000))
			.setReadTimeout(ofMillis(500000))
			.build();
	}
}
