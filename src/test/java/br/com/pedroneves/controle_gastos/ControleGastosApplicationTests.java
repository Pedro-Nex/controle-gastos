package br.com.pedroneves.controle_gastos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:controle_gastos_test",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"app.security.username=test-user",
		"app.security.password=test-password"
})
class ControleGastosApplicationTests {

	@Test
	void contextLoads() {
	}

}
