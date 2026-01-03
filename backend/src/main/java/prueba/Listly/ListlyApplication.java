package prueba.Listly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class ListlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ListlyApplication.class, args);
	}
}
