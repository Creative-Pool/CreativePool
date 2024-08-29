package com.creativepool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CreativePoolApplication {


	public static void main(String[] args) {
		SpringApplication.run(CreativePoolApplication.class, args);
	}


	@Bean
	public RestTemplate restTemplate(){
		return  new RestTemplate();
	}


}
