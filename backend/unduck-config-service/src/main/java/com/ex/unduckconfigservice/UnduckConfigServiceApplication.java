package com.ex.unduckconfigservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class UnduckConfigServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnduckConfigServiceApplication.class, args);
	}

}
