# PocketBase Java SDK

This is a Java SDK for PocketBase, implemented using OkHttp and Gson.

## Features

- Authentication with API keys
- User impersonation
- CRUD operations for records
- File uploads with multipart requests
- Helper methods for common tasks

## Usage Examples

### Initialize the SDK

```java
// From configuration
PocketBase pb = PocketBase.fromConfig();

// Manual initialization
PocketBase pb = new PocketBase("https://pocketbase.example.com", "YOUR_API_TOKEN");
```

### List Records

```java
try {
    // Basic list
    PocketBase.ListResult result = pb.listRecords("posts", 1, 20, null, null, null, null, false);
    List<JsonObject> posts = result.items;

    // With filtering and sorting
    PocketBase.ListResult filteredResult = pb.listRecords(
        "posts",
        1,                           // page
        20,                          // perPage
        "-created,title",            // sort by created desc, then title asc
        "status='published'",        // filter
        "author",                    // expand author relation
        "id,title,author.name",      // fields to return
        false                        // skipTotal
    );

    // Get full list (auto-paginated)
    List<JsonObject> allPosts = pb.getFullList("posts", 200, null, null, null, null);

    // Get first matching item
    JsonObject post = pb.getFirstListItem("posts", "title~'Java'", "-created", null, null);

} catch (IOException e) {
    e.printStackTrace();
}
```

### Get a Record

```java
try {
    JsonObject post = pb.getRecord("posts", "RECORD_ID", "author,comments", null);
    String title = PocketBaseUtils.getString(post, "title");
    System.out.println("Post title: " + title);

    // Convert to your own class
    MyPostClass myPost = PocketBaseUtils.jsonToClass(post, MyPostClass.class);
} catch (IOException e) {
    e.printStackTrace();
}
```

### Create a Record

```java
try {
    Map<String, Object> postData = new HashMap<>();
    postData.put("title", "New Post");
    postData.put("content", "This is a new post created with the Java SDK.");
    postData.put("status", "draft");

    JsonObject newPost = pb.createRecord("posts", postData, null, null);
    String postId = PocketBaseUtils.getString(newPost, "id");
    System.out.println("Created post with ID: " + postId);
} catch (IOException e) {
    e.printStackTrace();
}
```

### Update a Record

```java
try {
    Map<String, Object> updateData = new HashMap<>();
    updateData.put("title", "Updated Post Title");
    updateData.put("status", "published");

    JsonObject updatedPost = pb.updateRecord("posts", "RECORD_ID", updateData, null, null);
} catch (IOException e) {
    e.printStackTrace();
}
```

### Delete a Record

```java
try {
    pb.deleteRecord("posts", "RECORD_ID");
    System.out.println("Post deleted successfully");
} catch (IOException e) {
    e.printStackTrace();
}
```

### File Upload

```java
try {
    Map<String, Object> formData = new HashMap<>();
    formData.put("title", "Post with Image");

    Map<String, File> files = new HashMap<>();
    files.put("image", new File("/path/to/image.jpg"));

    RequestBody body = pb.createMultipartBody(formData, files);
    JsonObject newPost = pb.createRecord("posts", body, null, null);
} catch (IOException e) {
    e.printStackTrace();
}
```

### Impersonation

```java
try {
    // Impersonate a user with custom token duration (3600 seconds = 1 hour)
    PocketBase impersonated = pb.impersonate("users", "USER_ID", 3600, null, null);

    // Use the impersonated client to perform actions as that user
    JsonObject myData = impersonated.getRecord("my_collection", "RECORD_ID", null, null);

    // The original client is unaffected
    JsonObject adminData = pb.getRecord("admin_collection", "ADMIN_RECORD", null, null);
} catch (IOException e) {
    e.printStackTrace();
}
```
