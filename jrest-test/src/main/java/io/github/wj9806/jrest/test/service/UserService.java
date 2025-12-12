package io.github.wj9806.jrest.test.service;

import io.github.wj9806.jrest.client.annotation.RestClient;
import io.github.wj9806.jrest.client.annotation.DELETE;
import io.github.wj9806.jrest.client.annotation.GET;
import io.github.wj9806.jrest.client.annotation.POST;
import io.github.wj9806.jrest.client.annotation.PUT;
import io.github.wj9806.jrest.client.annotation.PathParam;
import io.github.wj9806.jrest.client.annotation.QueryParam;
import io.github.wj9806.jrest.client.annotation.Header;
import io.github.wj9806.jrest.client.annotation.RequestBody;
import io.github.wj9806.jrest.test.User;

@RestClient(baseUrl = "https://jsonplaceholder.typicode.com")
public interface UserService {
    
    @GET("/users")
    String getUsers(@QueryParam("name") String name);
    
    @GET("/users/{id}")
    String getUserById(@PathParam("id") Long id);
    
    @POST("/users")
    String createUser(@RequestBody User user, @Header("Authorization") String token);
    
    @PUT("/users/{id}")
    String updateUser(@PathParam("id") Long id, @RequestBody User user);
    
    @DELETE("/users/{id}")
    String deleteUser(@PathParam("id") Long id);
}
