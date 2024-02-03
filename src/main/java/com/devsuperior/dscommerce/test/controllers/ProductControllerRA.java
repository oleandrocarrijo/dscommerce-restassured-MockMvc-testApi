package com.devsuperior.dscommerce.test.controllers;

import com.devsuperior.dscommerce.tests.TokenUtil;
import groovy.json.JsonException;
import io.restassured.http.ContentType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;

public class ProductControllerRA {

    private String clientUsername, clientPassword, adminUsername, adminPassword;
    private String adminToken, clientToken, invalidToken;
    private Long existingProductId, nonExistingProductId, dependentProductId;
    private String productName;

    private Map<String, Object> postProductInstance;
    private Map<String, Object> putProductInstance;

    @BeforeEach
    public void setup() throws JsonException {
        baseURI = "http://localhost:8080";

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
        invalidToken = adminToken + "xpto";

        productName = "Macbook";
        existingProductId = 1L;

        postProductInstance = new HashMap<>();
        postProductInstance.put("name", "Me 123");
        postProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
        postProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
        postProductInstance.put("price", 20.0);

        putProductInstance = new HashMap<>();
        putProductInstance.put("name", "Produto atualizado");
        putProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
        putProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
        putProductInstance.put("price", 200.0);

        List<Map<String,Object>> categories = new ArrayList<>();

        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 2);

        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", 3);

        categories.add(category1);
        categories.add(category2);

        postProductInstance.put("categories", categories);
        putProductInstance.put("categories", categories);
    }

    @Test
    public void findByIdShouldReturnProductWhenIdExists() {

        existingProductId = 2L;

        given()
                .get("/products/{id}", existingProductId)
                .then()
                .statusCode(200)
                .body("id", is(2))
                .body("name", equalTo("Smart TV"))
                .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/" +
                        "dscatalog-resources/master/backend/img/2-big.jpg"))
                .body("price", is(2190.0F))
                .body("categories.id", hasItems(2,3))
                .body("categories.name", hasItems("Eletrônicos", "Computadores"));

    }

    @Test
    public void findAllShouldReturnPageProductsWhenProductNameIsEmpty() {

        given()
                .get("/products?page=0")
                .then()
                .statusCode(200)
                .body("content.name", hasItems("Macbook Pro", "PC Gamer Tera"))
                .body("content[0].name", is("The Lord of the Rings"));
    }

    @Test
    public void findAllShouldReturnPageProductsWhenProductNameIsNotEmpty() {

        given()
                .get("/products?name={productName}", productName)
                .then()
                .statusCode(200)
                .body("content.id[0]", is(3))
                .body("content.name[0]", equalTo("Macbook Pro"))
                .body("content.price[0]", is(1250.0F))
                .body("content.imgUrl[0]", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"));

    }

    @Test
    public void findAllShouldReturnPageProductsWithPriceGreaterThan2000() {

        given()
                .get("/products?size=25")
                .then()
                .statusCode(200)
                .body("content.findAll { it.price > 2000}.name", hasItems("Smart TV", "PC Gamer Weed"));
    }

    @Test
    public void insertShouldReturnProductCreatedWhenAdminsLogged() throws JsonException {

        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(201)
                .body("name", equalTo("Me 123"))
                .body("price", is(20.0f))
                .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
                .body("categories.id", hasItems(2, 3));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminsLoggedAndInvalidName() throws JsonException {
        postProductInstance.put("name", "ab");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Nome precisar ter de 3 a 80 caracteres"));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminsLoggedAndInvalidDescription() throws JsonException {
        postProductInstance.put("description", "ab");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Descrição precisa ter no mínimo 10 caracteres"));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminsLoggedAndPriceIsNegative() throws JsonException {
        postProductInstance.put("price", -1.0);
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("O preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminsLoggedAndPriceIsZero() throws JsonException {
        postProductInstance.put("price", 0.0);
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("O preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminsLoggedAndProductHasNoCategory() throws JsonException {
        postProductInstance.put("categories", null);
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Deve ter pelo menos uma categoria"));
    }

    @Test
    public void insertShouldReturnForbiddenWhenClientLogged() {

        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(403);
    }

    @Test
    public void insertShouldReturnUnauthorizedWhenInvalidToken() {

        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(401);
    }

    @Test
    public void deleteShouldReturnNoContentWhenAdminLoggedAndIdExists() {

        existingProductId = 10L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .delete("/products/{id}", existingProductId)
                .then()
                .statusCode(204);
    }

    @Test
    public void deleteShouldReturnNotFoundWhenAdminLoggedAndIdDoesNotExists() {

        nonExistingProductId = 250L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .delete("/products/{id}", nonExistingProductId)
                .then()
                .statusCode(404)
                .body("error", equalTo("Recurso não encontrado"))
                .body("status", equalTo(404));
    }

    @Test
    public void deleteShouldReturnBadRequestWhenAdminLoggedAndDependentId() {

        dependentProductId = 3L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .delete("/products/{id}", dependentProductId)
                .then()
                .statusCode(400);
    }

    @Test
    public void deleteShouldReturnForbiddenWhenClientLogged() {

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .delete("/products/{id}", existingProductId)
                .then()
                .statusCode(403);
    }

    @Test
    public void deleteShouldReturnUnauthorizedWhenClientLogged() {

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .delete("/products/{id}", existingProductId)
                .then()
                .statusCode(401);
    }
}
