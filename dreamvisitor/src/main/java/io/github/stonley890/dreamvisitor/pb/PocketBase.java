package io.github.stonley890.dreamvisitor.pb;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PocketBase {
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private final OkHttpClient client;
  private final Gson gson;
  private final String baseUrl;
  private String token;

  /**
   * Creates a new PocketBase SDK instance.
   *
   * @param baseUrl The base URL of the PocketBase instance
   * @param token   The admin/user auth token
   */
  public PocketBase(String baseUrl, String token) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    this.token = token;
    this.gson = new Gson();
    this.client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();
  }

  /**
   * Creates an instance with the default configuration from config.yml
   *
   * @return A preconfigured PocketBase instance
   */
  public static PocketBase fromConfig(Map<String, Object> config) {
    String baseUrl = (String) config.get("pocketbase-url");
    String token = (String) config.get("pocketbase-token");
    return new PocketBase(baseUrl, token);
  }

  /**
   * Helper method to build a URL with query parameters
   *
   * @param endpoint    The API endpoint
   * @param queryParams Query parameters
   * @return The complete URL
   */
  private HttpUrl buildUrl(String endpoint, @Nullable Map<String, String> queryParams) {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(this.baseUrl + endpoint).newBuilder();

    if (queryParams != null) {
      for (Map.Entry<String, String> entry : queryParams.entrySet()) {
        urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
      }
    }

    return urlBuilder.build();
  }

  /**
   * Helper method to execute HTTP requests
   *
   * @param method      HTTP method
   * @param endpoint    API endpoint
   * @param body        Request body
   * @param queryParams Query parameters
   * @return Response string
   * @throws IOException If the request fails
   */
  private String executeRequest(String method, String endpoint, @Nullable RequestBody body,
      @Nullable Map<String, String> queryParams) throws IOException {
    HttpUrl url = buildUrl(endpoint, queryParams);

    Request.Builder requestBuilder = new Request.Builder()
        .url(url)
        .header("Authorization", "Bearer " + this.token);

    switch (method) {
      case "GET":
        requestBuilder.get();
        break;
      case "POST":
        requestBuilder.post(body != null ? body : RequestBody.create(new byte[0], null));
        break;
      case "PATCH":
        requestBuilder.patch(body != null ? body : RequestBody.create(new byte[0], null));
        break;
      case "DELETE":
        requestBuilder.delete(body);
        break;
      default:
        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }

    Request request = requestBuilder.build();
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        String errorBody = response.body() != null ? response.body().string() : "No response body";
        throw new IOException("Request failed with code " + response.code() + ": " + errorBody);
      }

      return response.body() != null ? response.body().string() : "";
    }
  }

  /**
   * Impersonates a user and returns a new PocketBase instance with the
   * impersonation token
   *
   * @param collectionIdOrName Collection ID or name
   * @param recordId           Record ID to impersonate
   * @param duration           Optional JWT duration in seconds
   * @param expand             Optional relations to expand
   * @param fields             Optional fields to return
   * @return A new PocketBase instance with impersonation token
   * @throws IOException If the request fails
   */
  public PocketBase impersonate(String collectionIdOrName, String recordId,
      @Nullable Integer duration,
      @Nullable String expand, @Nullable String fields) throws IOException {
    String endpoint = "api/collections/" + collectionIdOrName + "/impersonate/" + recordId;

    Map<String, String> queryParams = new HashMap<>();
    if (expand != null)
      queryParams.put("expand", expand);
    if (fields != null)
      queryParams.put("fields", fields);

    JsonObject jsonBody = new JsonObject();
    if (duration != null)
      jsonBody.addProperty("duration", duration);

    RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
    String response = executeRequest("POST", endpoint, body, queryParams);

    ImpersonateResponse impersonateResponse = gson.fromJson(response, ImpersonateResponse.class);
    return new PocketBase(this.baseUrl, impersonateResponse.token);
  }

  /**
   * Lists/searches records from a collection
   *
   * @param collectionIdOrName Collection ID or name
   * @param page               Page number (default: 1)
   * @param perPage            Records per page (default: 30)
   * @param sort               Optional sorting
   * @param filter             Optional filter expression
   * @param expand             Optional relations to expand
   * @param fields             Optional fields to return
   * @param skipTotal          Whether to skip total counts
   * @return List response with records
   * @throws IOException If the request fails
   */
  public ListResult listRecords(String collectionIdOrName,
      @Nullable Integer page, @Nullable Integer perPage,
      @Nullable String sort, @Nullable String filter,
      @Nullable String expand, @Nullable String fields,
      @Nullable Boolean skipTotal) throws IOException {
    String endpoint = "api/collections/" + collectionIdOrName + "/records";

    Map<String, String> queryParams = new HashMap<>();
    if (page != null)
      queryParams.put("page", page.toString());
    if (perPage != null)
      queryParams.put("perPage", perPage.toString());
    if (sort != null)
      queryParams.put("sort", sort);
    if (filter != null)
      queryParams.put("filter", filter);
    if (expand != null)
      queryParams.put("expand", expand);
    if (fields != null)
      queryParams.put("fields", fields);
    if (skipTotal != null && skipTotal)
      queryParams.put("skipTotal", "true");

    String response = executeRequest("GET", endpoint, null, queryParams);
    return gson.fromJson(response, ListResult.class);
  }

  /**
   * Gets the full list of records from a collection (auto-paginated)
   *
   * @param collectionIdOrName Collection ID or name
   * @param batch              Batch size (default: 500)
   * @param sort               Optional sorting
   * @param filter             Optional filter expression
   * @param expand             Optional relations to expand
   * @param fields             Optional fields to return
   * @return List of records
   * @throws IOException If the request fails
   */
  public List<JsonObject> getFullList(String collectionIdOrName,
      @Nullable Integer batch,
      @Nullable String sort,
      @Nullable String filter,
      @Nullable String expand,
      @Nullable String fields) throws IOException {
    int batchSize = batch != null ? batch : 500;
    List<JsonObject> allItems = new ArrayList<>();
    int page = 1;

    while (true) {
      ListResult result = listRecords(
          collectionIdOrName,
          page,
          batchSize,
          sort,
          filter,
          expand,
          fields,
          true // skipTotal for optimization
      );

      allItems.addAll(result.items);

      if (result.items.size() < batchSize) {
        break;
      }

      page++;
    }

    return allItems;
  }

  /**
   * Gets the first record matching the filter
   *
   * @param collectionIdOrName Collection ID or name
   * @param filter             Filter expression
   * @param sort               Optional sorting
   * @param expand             Optional relations to expand
   * @param fields             Optional fields to return
   * @return First matching record
   * @throws IOException If the request fails or no record found
   */
  public JsonObject getFirstListItem(String collectionIdOrName,
      @NotNull String filter,
      @Nullable String sort,
      @Nullable String expand,
      @Nullable String fields) throws IOException {
    ListResult result = listRecords(
        collectionIdOrName,
        1,
        1,
        sort,
        filter,
        expand,
        fields,
        true // skipTotal for optimization
    );

    if (result.items.isEmpty()) {
      throw new IOException("No records found.");
    }

    return result.items.get(0);
  }

  /**
   * Gets a single record by ID
   *
   * @param collectionIdOrName Collection ID or name
   * @param recordId           Record ID
   * @param expand             Optional relations to expand
   * @param fields             Optional fields to return
   * @return Record data
   * @throws IOException If the request fails
   */
  public JsonObject getRecord(String collectionIdOrName, String recordId,
      @Nullable String expand, @Nullable String fields) throws IOException {
    String endpoint = "api/collections/" + collectionIdOrName + "/records/" + recordId;

    Map<String, String> queryParams = new HashMap<>();
    if (expand != null)
      queryParams.put("expand", expand);
    if (fields != null)
      queryParams.put("fields", fields);

    String response = executeRequest("GET", endpoint, null, queryParams);
    return gson.fromJson(response, JsonObject.class);
  }

  /**
   * Creates a new record
   *
   * @param collectionIdOrName Collection ID or name
   * @param bodyData           Record data
   * @param expand             Optional relations to expand
   * @param fields             Optional fields to return
   * @return Created record
   * @throws IOException If the request fails
   */
  public JsonObject createRecord(String collectionIdOrName, Object bodyData,
      @Nullable String expand, @Nullable String fields) throws IOException {
    String endpoint = "api/collections/" + collectionIdOrName + "/records";

    Map<String, String> queryParams = new HashMap<>();
    if (expand != null)
      queryParams.put("expand", expand);
    if (fields != null)
      queryParams.put("fields", fields);

    RequestBody body = RequestBody.create(gson.toJson(bodyData), JSON);
    String response = executeRequest("POST", endpoint, body, queryParams);
    return gson.fromJson(response, JsonObject.class);
  }

  /**
   * Updates an existing record
   *
   * @param collectionIdOrName Collection ID or name
   * @param recordId           Record ID
   * @param bodyData           Updated data
   * @param expand             Optional relations to expand
   * @param fields             Optional fields to return
   * @return Updated record
   * @throws IOException If the request fails
   */
  public JsonObject updateRecord(String collectionIdOrName, String recordId, Object bodyData,
      @Nullable String expand, @Nullable String fields) throws IOException {
    String endpoint = "api/collections/" + collectionIdOrName + "/records/" + recordId;

    Map<String, String> queryParams = new HashMap<>();
    if (expand != null)
      queryParams.put("expand", expand);
    if (fields != null)
      queryParams.put("fields", fields);

    RequestBody body = RequestBody.create(gson.toJson(bodyData), JSON);
    String response = executeRequest("PATCH", endpoint, body, queryParams);
    return gson.fromJson(response, JsonObject.class);
  }

  /**
   * Deletes a record
   *
   * @param collectionIdOrName Collection ID or name
   * @param recordId           Record ID
   * @throws IOException If the request fails
   */
  public void deleteRecord(String collectionIdOrName, String recordId) throws IOException {
    String endpoint = "api/collections/" + collectionIdOrName + "/records/" + recordId;
    executeRequest("DELETE", endpoint, null, null);
  }

  /**
   * Creates a multipart request body for file uploads
   *
   * @param fields Map of field names to values
   * @param files  Map of field names to files
   * @return Multipart request body
   */
  public RequestBody createMultipartBody(Map<String, Object> fields, Map<String, File> files) {
    MultipartBody.Builder builder = new MultipartBody.Builder()
        .setType(MultipartBody.FORM);

    // Add regular fields
    for (Map.Entry<String, Object> entry : fields.entrySet()) {
      builder.addFormDataPart(entry.getKey(), String.valueOf(entry.getValue()));
    }

    // Add files
    for (Map.Entry<String, File> entry : files.entrySet()) {
      String fieldName = entry.getKey();
      File file = entry.getValue();
      String fileName = file.getName();
      RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
      builder.addFormDataPart(fieldName, fileName, fileBody);
    }

    return builder.build();
  }

  /**
   * Get the current auth token
   *
   * @return Current auth token
   */
  public String getToken() {
    return token;
  }

  /**
   * Update the auth token
   *
   * @param token New auth token
   */
  public void setToken(String token) {
    this.token = token;
  }

  /**
   * Response class for impersonation
   */
  private static class ImpersonateResponse {
    String token;
    JsonObject record;
  }

  /**
   * Response class for list operations
   */
  public static class ListResult {
    public int page;
    public int perPage;
    public int totalItems;
    public int totalPages;
    public List<JsonObject> items;
  }
}
