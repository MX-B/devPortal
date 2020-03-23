package io.gr1d.ic.usage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@ComponentScan("io.gr1d")
@EnableFeignClients("io.gr1d.ic.usage.api")
@SpringBootApplication
public class Gr1dDevportalUsageServiceApplication {
	
	public static void main(final String[] args) {
		SpringApplication.run(Gr1dDevportalUsageServiceApplication.class, args);
	}
}
