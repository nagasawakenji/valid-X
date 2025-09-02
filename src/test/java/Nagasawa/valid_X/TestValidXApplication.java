package Nagasawa.valid_X;

import org.springframework.boot.SpringApplication;

public class TestValidXApplication {

	public static void main(String[] args) {
		SpringApplication.from(ValidXApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
