package com.example.itsm_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class ItsmApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItsmApiApplication.class, args);
	}

}
