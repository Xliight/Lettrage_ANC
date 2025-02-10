package lettrage.example.lettrage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class LettrageApplication {

	public static void main(String[] args) {
		SpringApplication.run(LettrageApplication.class, args);
	}

}
