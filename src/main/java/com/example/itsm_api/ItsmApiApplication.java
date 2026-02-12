package com.example.itsm_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableAspectJAutoProxy
@MapperScan({"com.example.itsm_api.mapper","com.example.itsm_api.cmmncode.dao"})
public class ItsmApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItsmApiApplication.class, args);
	}

}
