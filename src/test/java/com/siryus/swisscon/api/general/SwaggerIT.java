package com.siryus.swisscon.api.general;


import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SwaggerIT extends AbstractMvcTestBase {


	private static final String URL_SWAGGER_JSON = "/v2/api-docs";
	private static final String URL_SWAGGER_UI = "/swagger-ui.html";


	@Test
	public void testQuerySwaggerJSON() {
		String json = RestAssured.given()
				.spec(this.defaultSpec())
				.get(URL_SWAGGER_JSON).then()
				.statusCode(200).extract().asString();
		assertNotNull(json);
		assertThat(json, containsString("\"swagger\":\"2.0\""));
	}

	@Test
	public void testQuerySwaggerUI() {
		String json = RestAssured.given()
				.spec(this.defaultSpec())
				.get(URL_SWAGGER_UI).then()
				.statusCode(200).extract().asString();
		assertNotNull(json);
		assertThat(json, containsString("<title>Swagger UI</title>"));
	}
}
