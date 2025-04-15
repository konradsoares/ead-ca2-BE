package com.example.ead.be;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component
public class Persistence {
    @Value("${spring.data.mongodb.uri}")
    private String connString;

    private final String dbName = "ead_ca2";
    private final String collectionName = "ead_2024";

    private MongoClient mongoClient = null;
    private MongoDatabase database = null;
    private MongoCollection<Recipe> collection = null;

    public static List<Recipe> recipes = Arrays.asList(
            new Recipe("elotes", Arrays.asList("corn", "mayonnaise", "cotija cheese", "sour cream", "lime"), 35),
            new Recipe("loco moco", Arrays.asList("ground beef", "butter", "onion", "egg", "bread bun", "mushrooms"), 54),
            new Recipe("patatas bravas", Arrays.asList("potato", "tomato", "olive oil", "onion", "garlic", "paprika"), 80),
            new Recipe("fried rice", Arrays.asList("rice", "soy sauce", "egg", "onion", "pea", "carrot", "sesame oil"), 40)
    );

    @PostConstruct
    public void init() {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);
        ConnectionString mongoUri = new ConnectionString(connString);

        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .applyConnectionString(mongoUri).build();

        try {
            mongoClient = MongoClients.create(settings);
        } catch (MongoException me) {
            System.err.println("Unable to connect to MongoDB: " + me);
            System.exit(1);
        }

        database = mongoClient.getDatabase(dbName);
        collection = database.getCollection(collectionName, Recipe.class);
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> myRecipes = new ArrayList<>();
        try (MongoCursor<Recipe> cur = collection.find().iterator()) {
            while (cur.hasNext()) {
                myRecipes.add(cur.next());
            }
        }
        return myRecipes;
    }

    public int addRecipes(List<Recipe> recipes) {
        try {
            InsertManyResult result = collection.insertMany(recipes);
            System.out.println("Inserted " + result.getInsertedIds().size() + " documents.\n");
            return result.getInsertedIds().size();
        } catch (MongoException me) {
            System.err.println("Insert error: " + me);
            return -1;
        }
    }

    public int deleteRecipes(Bson filter) {
        try {
            DeleteResult result = collection.deleteMany(filter);
            System.out.printf("\nDeleted %d documents.\n", result.getDeletedCount());
            return (int) result.getDeletedCount();
        } catch (MongoException me) {
            System.err.println("Delete error: " + me);
            return -1;
        }
    }

    public int deleteRecipesByName(List<String> names) {
        return deleteRecipes(Filters.in("name", names));
    }
}
