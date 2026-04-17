package com.cis.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MongoUser {
    @Id
    private UUID id;
    private String name;
    private String login;
    private String password;
}
