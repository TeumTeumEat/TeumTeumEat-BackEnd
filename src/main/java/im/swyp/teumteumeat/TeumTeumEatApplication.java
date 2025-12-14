package im.swyp.teumteumeat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TeumTeumEatApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeumTeumEatApplication.class, args);
	}

}
