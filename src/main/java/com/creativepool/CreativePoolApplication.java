package com.creativepool;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class CreativePoolApplication {


	public static void main(String[] args) {
		listFilesRecursively(new File("/workspace"));
		SpringApplication.run(CreativePoolApplication.class, args);
	}

	private static void listFilesRecursively(File directory) {
		if (directory != null && directory.isDirectory()) {
			File[] files = directory.listFiles();
			if (files != null) {
				for (File file : files) {
					System.out.println(file.getAbsolutePath());
					if (file.isDirectory()) {
						listFilesRecursively(file);
					}
				}
			}
		}
	}


	@Value("${credential.file}")
	private String credentialsFilePath;

	@Bean
	public RestTemplate restTemplate() throws IOException {
		// Load the credentials file from the specified path

		InputStream stream = getClass().getClassLoader().getResourceAsStream(credentialsFilePath);
		GoogleCredentials credentials = GoogleCredentials
				.fromStream(stream)
				.createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

		return new RestTemplateBuilder()
				.interceptors((request, body, execution) -> {
					// Refresh the credentials if the access token is expired
					credentials.refreshIfExpired();
					// Set the Bearer token for authorization
					request.getHeaders().setBearerAuth(credentials.getAccessToken().getTokenValue());
					return execution.execute(request, body);
				})
				.build();

//		GoogleCredentials credentials = GoogleCredentials
//				.fromStream(new FileInputStream("src/main/resources/your-credentials.json"))
//				.createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-platform"));
//
//		// Create an HttpClient with the credentials
//		CloseableHttpClient httpClient = HttpClients.custom()
//				.addInterceptorFirst(new GoogleCredentialsHttpRequestInterceptor(credentials))
//				.build();
//
//		// Create and return a RestTemplate
//		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
		 }


}
