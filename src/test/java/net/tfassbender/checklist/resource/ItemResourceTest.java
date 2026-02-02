package net.tfassbender.checklist.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class ItemResourceTest {

    private String authToken;
    private String username = "testuser";
    private String listId;

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
                .post("/api/auth/register");

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

        // Create a test list
        Response createListResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Test List"
                        }
                        """)
                .post("/api/lists");

        listId = createListResponse.jsonPath().getString("id");
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
    void testAddItem() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Buy milk"
                        }
                        """)
                .when()
                .post("/api/lists/" + listId + "/items")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("description", equalTo("Buy milk"))
                .body("checked", equalTo(false))
                .body("orderIndex", notNullValue());
    }

    @Test
    void testAddItemUnauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Buy milk"
                        }
                        """)
                .when()
                .post("/api/lists/" + listId + "/items")
                .then()
                .statusCode(401);
    }

    @Test
    void testAddItemEmptyDescription() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": ""
                        }
                        """)
                .when()
                .post("/api/lists/" + listId + "/items")
                .then()
                .statusCode(400)
                .body("message", containsString("description"))
                .body("status", equalTo(400));
    }

    @Test
    void testAddItemToNonexistentList() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Buy milk"
                        }
                        """)
                .when()
                .post("/api/lists/nonexistent-id/items")
                .then()
                .statusCode(404)
                .body("message", containsString("not found"))
                .body("status", equalTo(404));
    }

    @Test
    void testAddMultipleItems() {
        // Add first item
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Buy milk"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        // Add second item
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Buy bread"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        // Verify both items are in the list
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/lists/" + listId)
                .then()
                .statusCode(200)
                .body("items.size()", equalTo(2))
                .body("items.description", hasItems("Buy milk", "Buy bread"));
    }

    @Test
    void testUpdateItem() {
        // Add an item
        Response addResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Buy milk"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        String itemId = addResponse.jsonPath().getString("id");

        // Update the item
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Buy almond milk",
                            "checked": true
                        }
                        """)
                .when()
                .put("/api/lists/" + listId + "/items/" + itemId)
                .then()
                .statusCode(200)
                .body("id", equalTo(itemId))
                .body("description", equalTo("Buy almond milk"))
                .body("checked", equalTo(true));
    }

    @Test
    void testUpdateItemNotFound() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Updated description",
                            "checked": true
                        }
                        """)
                .when()
                .put("/api/lists/" + listId + "/items/nonexistent-id")
                .then()
                .statusCode(404)
                .body("message", containsString("not found"))
                .body("status", equalTo(404));
    }

    @Test
    void testUpdateItemInNonexistentList() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Updated description",
                            "checked": true
                        }
                        """)
                .when()
                .put("/api/lists/nonexistent-list/items/some-item-id")
                .then()
                .statusCode(404)
                .body("message", containsString("not found"))
                .body("status", equalTo(404));
    }

    @Test
    void testUpdateItemEmptyDescription() {
        // Add an item
        Response addResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Buy milk"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        String itemId = addResponse.jsonPath().getString("id");

        // Try to update with empty description
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "",
                            "checked": true
                        }
                        """)
                .when()
                .put("/api/lists/" + listId + "/items/" + itemId)
                .then()
                .statusCode(400)
                .body("message", containsString("description"))
                .body("status", equalTo(400));
    }

    @Test
    void testDeleteItem() {
        // Add an item
        Response addResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Buy milk"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        String itemId = addResponse.jsonPath().getString("id");

        // Delete the item
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete("/api/lists/" + listId + "/items/" + itemId)
                .then()
                .statusCode(204);

        // Verify the item is deleted
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/lists/" + listId)
                .then()
                .statusCode(200)
                .body("items.size()", equalTo(0));
    }

    @Test
    void testDeleteItemNotFound() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete("/api/lists/" + listId + "/items/nonexistent-id")
                .then()
                .statusCode(404)
                .body("message", containsString("not found"))
                .body("status", equalTo(404));
    }

    @Test
    void testDeleteItemInNonexistentList() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete("/api/lists/nonexistent-list/items/some-item-id")
                .then()
                .statusCode(404)
                .body("message", containsString("not found"))
                .body("status", equalTo(404));
    }

    @Test
    void testReorderItems() {
        // Add three items
        Response item1 = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "First item"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        Response item2 = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Second item"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        Response item3 = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Third item"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        String item1Id = item1.jsonPath().getString("id");
        String item2Id = item2.jsonPath().getString("id");
        String item3Id = item3.jsonPath().getString("id");

        // Reorder items (reverse order)
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                            "itemIds": ["%s", "%s", "%s"]
                        }
                        """, item3Id, item2Id, item1Id))
                .when()
                .put("/api/lists/" + listId + "/items/reorder")
                .then()
                .statusCode(200)
                .body("items.size()", equalTo(3))
                .body("items[0].id", equalTo(item3Id))
                .body("items[1].id", equalTo(item2Id))
                .body("items[2].id", equalTo(item1Id));
    }

    @Test
    void testReorderItemsListNotFound() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "itemIds": ["id1", "id2"]
                        }
                        """)
                .when()
                .put("/api/lists/nonexistent-list/items/reorder")
                .then()
                .statusCode(404)
                .body("message", containsString("not found"))
                .body("status", equalTo(404));
    }

    @Test
    void testReorderItemsWithPartialList() {
        // Add three items
        Response item1 = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "First item"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        Response item2 = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Second item"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        Response item3 = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Third item"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        String item1Id = item1.jsonPath().getString("id");
        String item2Id = item2.jsonPath().getString("id");
        String item3Id = item3.jsonPath().getString("id");

        // Reorder with partial list (only 2 of 3 items)
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                            "itemIds": ["%s", "%s"]
                        }
                        """, item2Id, item1Id))
                .when()
                .put("/api/lists/" + listId + "/items/reorder")
                .then()
                .statusCode(200);
    }

    @Test
    void testItemIsolationBetweenUsers() {
        // Add an item as user 1
        Response addResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "User 1 item"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        String itemId = addResponse.jsonPath().getString("id");

        // Register and login as user 2
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

        // User 2 should not be able to access User 1's list items
        given()
                .header("Authorization", "Bearer " + user2Token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Updated description",
                            "checked": true
                        }
                        """)
                .when()
                .put("/api/lists/" + listId + "/items/" + itemId)
                .then()
                .statusCode(404);

        // User 2 should not be able to delete User 1's items
        given()
                .header("Authorization", "Bearer " + user2Token)
                .when()
                .delete("/api/lists/" + listId + "/items/" + itemId)
                .then()
                .statusCode(404);
    }

    @Test
    void testCheckAndUncheckItem() {
        // Add an item
        Response addResponse = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Buy milk"
                        }
                        """)
                .post("/api/lists/" + listId + "/items");

        String itemId = addResponse.jsonPath().getString("id");

        // Check the item
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Buy milk",
                            "checked": true
                        }
                        """)
                .when()
                .put("/api/lists/" + listId + "/items/" + itemId)
                .then()
                .statusCode(200)
                .body("checked", equalTo(true));

        // Uncheck the item
        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "description": "Buy milk",
                            "checked": false
                        }
                        """)
                .when()
                .put("/api/lists/" + listId + "/items/" + itemId)
                .then()
                .statusCode(200)
                .body("checked", equalTo(false));
    }
}
