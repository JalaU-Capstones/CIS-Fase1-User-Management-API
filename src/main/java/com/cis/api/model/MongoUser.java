package com.cis.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class MongoUser {

    @Id
    @Field("_id")
    private String id;

    @Field("name")
    private String name;

    @Indexed(unique = true)
    @Field("login")
    private String login;

    @Field("password")
    private String password;

    private Long migratedAt;
    private String source = "mysql";

    public MongoUser(String id, String name, String login, String password) {
        this.id = id;
        this.name = name;
        this.login = login;
        this.password = password;
        this.migratedAt = System.currentTimeMillis();
    }
}