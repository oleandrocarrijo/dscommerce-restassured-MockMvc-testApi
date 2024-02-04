package com.devsuperior.dscommerce.controllers;

import com.devsuperior.dscommerce.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

public class OrderControllerRA {

    private String clientUsername, clientPassword, adminUsername, adminPassword;
    private String clientToken, adminToken, invalidToken;
    private Long existingOrderId, nonexistingOrderId, otherOderId;

    @BeforeEach
    void setUp() {
        baseURI = "http://localhost:8080";

        existingOrderId = 1L;
        otherOderId = 2L;
        nonexistingOrderId = 100L;

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
        invalidToken = adminToken + "xpto";
    }

    @Test
    public void findByIdShouldReturnOrderWhenIdExistsAndAdminLogged() {

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("moment", equalTo("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("client.name", equalTo( "Maria Brown"))
                .body("payment.moment", equalTo( "2022-07-25T15:00:00Z"))
                .body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"))
                .body("total", is(1431.0F));

    }

    @Test
    public void findByIdShouldReturnOrderWhenIdExistsAndClientLogged() {

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("moment", equalTo("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("client.name", equalTo( "Maria Brown"))
                .body("payment.moment", equalTo( "2022-07-25T15:00:00Z"))
                .body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"))
                .body("total", is(1431.0F));

    }

    @Test
    public void findByIdShouldReturnForbiddenWhenIdExistsAndClientLoggedAndOrderDoesNotBelongUser() {

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", otherOderId)
                .then()
                .statusCode(403);
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistsAndClientLogged() {

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", nonexistingOrderId)
                .then()
                .statusCode(404);
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistsAndAdminLogged() {

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", nonexistingOrderId)
                .then()
                .statusCode(404);
    }

    @Test
    public void findByIdShouldReturnUnauthorizedWhenIdExistsAndInvalidToken() {

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(401);
    }
}
