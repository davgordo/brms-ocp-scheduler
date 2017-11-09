package com.raffa.brmsscheduler;

import javax.inject.Inject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
 
public class BrmsOcpSchedulerApplication {
	@Inject
	PodWatcherFabric8 pwf8;

	public static void main(String[] args) {
		SpringApplication.run(BrmsOcpSchedulerApplication.class, args);
	}
	
	@Bean
	public PodWatcherFabric8 pwf8() {
		return new PodWatcherFabric8();
	}
}
