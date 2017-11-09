package com.raffa.brmsscheduler;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.apis.CoreV1Api;

@Configuration
public class KubernetesConfig {
	
	
	@Bean
	public ApiClient client() throws IOException {
		ApiClient client=Config.defaultClient();
		io.kubernetes.client.Configuration.setDefaultApiClient(client);
		return client;
	}
	
	@Bean
    public CoreV1Api v1Api(ApiClient client) {
		return new CoreV1Api();
	}
}
