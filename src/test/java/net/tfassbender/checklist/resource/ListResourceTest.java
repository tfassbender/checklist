package net.tfassbender.checklist.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class ListResourceTest {

    private String authToken;
    private String username = "testuser";

    @BeforeEach
    void setUp() {
        // Clean up test data directory before each test
        File dataDir = new File("target/test-data");
        if (dataDir.exists()) {
            deleteDirectory(dataDir);
        }
        dataDir.mkdirs();

        // Register and login to get auth token
        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                            "username": "%s",
                            "password": "password123"
                        }
                        """, username))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200);

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                            "username": "%s",
                            "password": "password123"
                        }
                        """, username))
                .when()
                .post("/api/auth/login");

        authToken = loginResponse.jsonPath().getString("token");
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
    void testGetAllListsEmpty() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/lists")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void testGetAllListsUnauthorized() {
        given()
                .when()
                .get("/api/lists")
                .then()
                .statusCode(401);
    }

    @Test
    void testCreateList() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Shopping List"
                        }
                        """)
                .when()
                .post("/api/lists")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Shopping List"))
                .body("notes", equalTo(""))
                .body("items", notNullValue())
                .body("items.size()", equalTo(0));
    }

    @Test
    void testCreateListEmptyName() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": ""
                        }
                        """)
                .when()
                .post("/api/lists")
                .then()
                .statusCode(400)
                .body("message", containsString("name"))
                .body("status", equalTo(400));
    }

    @Test
    void testCreateDuplicateList() {
        // Create first list
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Shopping List"
                        }
                        """)
                .when()
                .post("/api/lists")
                .then()
                .statusCode(201);

        // Try to create duplicate
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Shopping List"
                        }
                        """)
                .when()
                .post("/api/lists")
                .then()
                .statusCode(409)
                .body("message", containsString("already exists"))
                .body("status", equalTo(409));
    }

    @Test
    void testGetAllLists() {
        // Create multiple lists
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Shopping List"
                        }
                        """)
                .post("/api/lists");

        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Todo List"
                        }
                        """)
                .post("/api/lists");

        // Get all lists
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/lists")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("name", hasItems("Shopping List", "Todo List"));
    }

    @Test
    void testGetListById() {
        // Create a list
        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Shopping List"
                        }
                        """)
                .post("/api/lists");

        String listId = createResponse.jsonPath().getString("id");

        // Get the list by ID
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/lists/" + listId)
                .then()
                .statusCode(200)
                .body("id", equalTo(listId))
                .body("name", equalTo("Shopping List"));
    }

    @Test
    void testGetListByIdNotFound() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/lists/nonexistent-id")
                .then()
                .statusCode(404)
                .body("message", containsString("not found"))
                .body("status", equalTo(404));
    }

    @Test
    void testUpdateList() {
        // Create a list
        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Shopping List"
                        }
                        """)
                .post("/api/lists");

        String listId = createResponse.jsonPath().getString("id");

        // Update the list
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Updated Shopping List",
                            "notes": "Don't forget milk!"
                        }
                        """)
                .when()
                .put("/api/lists/" + listId)
                .then()
                .statusCode(200)
                .body("id", equalTo(listId))
                .body("name", equalTo("Updated Shopping List"))
                .body("notes", equalTo("Don't forget milk!"));
    }

    @Test
    void testUpdateListNotFound() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Updated Name",
                            "notes": "Notes"
                        }
                        """)
                .when()
                .put("/api/lists/nonexistent-id")
                .then()
                .statusCode(404)
                .body("message", containsString("not found"))
                .body("status", equalTo(404));
    }

    @Test
    void testUpdateListDuplicateName() {
        // Create two lists
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "List One"
                        }
                        """)
                .post("/api/lists");

        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "List Two"
                        }
                        """)
                .post("/api/lists");

        String listTwoId = createResponse.jsonPath().getString("id");

        // Try to update List Two to have the same name as List One
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "List One",
                            "notes": "Notes"
                        }
                        """)
                .when()
                .put("/api/lists/" + listTwoId)
                .then()
                .statusCode(409)
                .body("message", containsString("already exists"))
                .body("status", equalTo(409));
    }

    @Test
    void testDeleteList() {
        // Create a list
        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Shopping List"
                        }
                        """)
                .post("/api/lists");

        String listId = createResponse.jsonPath().getString("id");

        // Delete the list
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete("/api/lists/" + listId)
                .then()
                .statusCode(204);

        // Verify it's deleted
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/lists/" + listId)
                .then()
                .statusCode(404);
    }

    @Test
    void testDeleteListNotFound() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete("/api/lists/nonexistent-id")
                .then()
                .statusCode(404)
                .body("status", equalTo(404));
    }

    @Test
    void testResetList() {
        // Create a list
        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Shopping List"
                        }
                        """)
                .post("/api/lists");

        String listId = createResponse.jsonPath().getString("id");

        // Add some items
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Milk"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        Response itemResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Bread"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        String itemId = itemResponse.jsonPath().getString("id");

        // Check one item
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Bread",
                            "checked": true
                        }
                        """)
                .put("/api/lists/" + listId + "/items/" + itemId);

        // Reset the list
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .post("/api/lists/" + listId + "/reset")
                .then()
                .statusCode(200)
                .body("items.size()", equalTo(2))
                .body("items.every { it.checked == false }", equalTo(true));
    }

    @Test
    void testResetListNotFound() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .post("/api/lists/nonexistent-id/reset")
                .then()
                .statusCode(404)
                .body("status", equalTo(404));
    }

    @Test
    void testUserIsolation() {
        // Create a list with first user
        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "User1 List"
                        }
                        """)
                .post("/api/lists");

        String listId = createResponse.jsonPath().getString("id");

        // Register and login as a different user
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "user2",
                            "password": "password123"
                        }
                        """)
                .post("/api/auth/register");

        Response user2LoginResponse = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "user2",
                            "password": "password123"
                        }
                        """)
                .post("/api/auth/login");

        String user2Token = user2LoginResponse.jsonPath().getString("token");

        // User 2 should not be able to access User 1's list
        given()
                .header("Authorization", "Bearer " + user2Token)
                .when()
                .get("/api/lists/" + listId)
                .then()
                .statusCode(404);

        // User 2 should not see User 1's list in their list
        given()
                .header("Authorization", "Bearer " + user2Token)
                .when()
                .get("/api/lists")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void testDeactivateList() {
        // Create a list
        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Test List"
                        }
                        """)
                .post("/api/lists");

        String listId = createResponse.jsonPath().getString("id");

        // Add some items
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Item 1"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        Response item2Response = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Item 2"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        String item2Id = item2Response.jsonPath().getString("id");

        // Check one item
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Item 2",
                            "checked": true
                        }
                        """)
                .put("/api/lists/" + listId + "/items/" + item2Id);

        // Deactivate the list
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .put("/api/lists/" + listId + "/deactivate")
                .then()
                .statusCode(200)
                .body("active", equalTo(false))
                .body("items.size()", equalTo(2))
                .body("items.every { it.checked == false }", equalTo(true));
    }

    @Test
    void testDeactivateListIsIdempotent() {
        // Create and deactivate a list
        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Test List"
                        }
                        """)
                .post("/api/lists");

        String listId = createResponse.jsonPath().getString("id");

        // Deactivate once
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .put("/api/lists/" + listId + "/deactivate")
                .then()
                .statusCode(200)
                .body("active", equalTo(false));

        // Deactivate again - should still succeed and be inactive
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .put("/api/lists/" + listId + "/deactivate")
                .then()
                .statusCode(200)
                .body("active", equalTo(false));
    }

    @Test
    void testActivateList() {
        // Create and deactivate a list
        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Test List"
                        }
                        """)
                .post("/api/lists");

        String listId = createResponse.jsonPath().getString("id");

        // Deactivate
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .put("/api/lists/" + listId + "/deactivate");

        // Activate the list
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .put("/api/lists/" + listId + "/activate")
                .then()
                .statusCode(200)
                .body("active", equalTo(true));
    }

    @Test
    void testActivateListIsIdempotent() {
        // Create a list (already active by default)
        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Test List"
                        }
                        """)
                .post("/api/lists");

        String listId = createResponse.jsonPath().getString("id");

        // Activate when already active - should still succeed
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .put("/api/lists/" + listId + "/activate")
                .then()
                .statusCode(200)
                .body("active", equalTo(true));

        // Activate again - should still succeed
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .put("/api/lists/" + listId + "/activate")
                .then()
                .statusCode(200)
                .body("active", equalTo(true));
    }

    @Test
    void testActivateListNotFound() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .put("/api/lists/nonexistent-id/activate")
                .then()
                .statusCode(404)
                .body("status", equalTo(404));
    }

    @Test
    void testDeactivateListNotFound() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .when()
                .put("/api/lists/nonexistent-id/deactivate")
                .then()
                .statusCode(404)
                .body("status", equalTo(404));
    }

    @Test
    void testActivateDeactivateCycle() {
        // Create a list
        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Test List"
                        }
                        """)
                .post("/api/lists");

        String listId = createResponse.jsonPath().getString("id");

        // Verify initially active
        given()
                .header("Authorization", "Bearer " + authToken)
                .get("/api/lists/" + listId)
                .then()
                .statusCode(200)
                .body("active", equalTo(true));

        // Deactivate
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .put("/api/lists/" + listId + "/deactivate")
                .then()
                .statusCode(200)
                .body("active", equalTo(false));

        // Activate again
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .put("/api/lists/" + listId + "/activate")
                .then()
                .statusCode(200)
                .body("active", equalTo(true));

        // Deactivate again
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .put("/api/lists/" + listId + "/deactivate")
                .then()
                .statusCode(200)
                .body("active", equalTo(false));
    }

    @Test
    void testDeactivateResetsCheckedItems() {
        // Create a list with checked items
        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Test List"
                        }
                        """)
                .post("/api/lists");

        String listId = createResponse.jsonPath().getString("id");

        // Add items and check them
        Response item1Response = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Item 1"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        String item1Id = item1Response.jsonPath().getString("id");

        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Item 1",
                            "checked": true
                        }
                        """)
                .put("/api/lists/" + listId + "/items/" + item1Id);

        // Verify item is checked
        given()
                .header("Authorization", "Bearer " + authToken)
                .get("/api/lists/" + listId)
                .then()
                .body("items[0].checked", equalTo(true));

        // Deactivate - should reset items
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .put("/api/lists/" + listId + "/deactivate")
                .then()
                .statusCode(200)
                .body("active", equalTo(false))
                .body("items[0].checked", equalTo(false));
    }

    @Test
    void testActivateDoesNotResetItems() {
        // Create, add items, check them, deactivate, then activate
        Response createResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Test List"
                        }
                        """)
                .post("/api/lists");

        String listId = createResponse.jsonPath().getString("id");

        // Add item (will be unchecked by default after we deactivate)
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Item 1"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        // Deactivate (resets items to unchecked)
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .put("/api/lists/" + listId + "/deactivate");

        // Activate - should NOT change item checked state
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .put("/api/lists/" + listId + "/activate")
                .then()
                .statusCode(200)
                .body("active", equalTo(true))
                .body("items[0].checked", equalTo(false));
    }
}
