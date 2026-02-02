package net.tfassbender.checklist.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class AuthResourceTest {

    @BeforeEach
    void setUp() {
        // Clean up test data directory before each test
        File dataDir = new File("target/test-data");
        if (dataDir.exists()) {
            deleteDirectory(dataDir);
        }
        dataDir.mkdirs();
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                }
                file.delete();
            }
        }
    }

    @Test
    void testRegisterSuccess() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "testuser",
                            "password": "password123"
                        }
                        """)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("username", equalTo("testuser"));
    }

    @Test
    void testRegisterWithWhitespace() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "  TestUser  ",
                            "password": "password123"
                        }
                        """)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("username", equalTo("TestUser"));
    }

    @Test
    void testRegisterDuplicateUsername() {
        // First registration
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "testuser",
                            "password": "password123"
                        }
                        """)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200);

        // Duplicate registration
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "testuser",
                            "password": "password456"
                        }
                        """)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("message", containsString("already exists"))
                .body("status", equalTo(400));
    }

    @Test
    void testRegisterEmptyUsername() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "",
                            "password": "password123"
                        }
                        """)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("message", containsString("Username"))
                .body("status", equalTo(400));
    }

    @Test
    void testRegisterEmptyPassword() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "testuser",
                            "password": ""
                        }
                        """)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("message", containsString("Password"))
                .body("status", equalTo(400));
    }

    @Test
    void testLoginSuccess() {
        // Register user first
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "testuser",
                            "password": "password123"
                        }
                        """)
                .when()
                .post("/api/auth/register");

        // Login
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "testuser",
                            "password": "password123"
                        }
                        """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("username", equalTo("testuser"));
    }

    @Test
    void testLoginCaseInsensitive() {
        // Register user with lowercase
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "testuser",
                            "password": "password123"
                        }
                        """)
                .when()
                .post("/api/auth/register");

        // Login with uppercase
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "TestUser",
                            "password": "password123"
                        }
                        """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("username", equalTo("testuser"));
    }

    @Test
    void testLoginInvalidUsername() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "nonexistent",
                            "password": "password123"
                        }
                        """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("message", containsString("Invalid"))
                .body("status", equalTo(401));
    }

    @Test
    void testLoginInvalidPassword() {
        // Register user
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "testuser",
                            "password": "password123"
                        }
                        """)
                .when()
                .post("/api/auth/register");

        // Login with wrong password
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "testuser",
                            "password": "wrongpassword"
                        }
                        """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("message", containsString("Invalid"))
                .body("status", equalTo(401));
    }

    @Test
    void testLoginEmptyCredentials() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "",
                            "password": ""
                        }
                        """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("status", equalTo(401));
    }
}
