package com.librarymanagement.librarymanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class LibrarymanagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibrarymanagementApplication.class, args);
	}

}
