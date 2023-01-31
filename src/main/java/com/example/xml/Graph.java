package com.example.xml;

import java.util.ArrayList;

public class Graph { // A class that represents the social network graph

    private final ArrayList<User> users; // User's list

    Graph() {
        users = new ArrayList<>();
    } // initialize the users list

    void addUser(User node) {
        users.add(node);
    } // adding a user to the users list

    ArrayList<User> getUsers() {
        return users;
    } // returns all the users in the graph
}

class User { // A class that represents a user in the graph

    private int follows = 0; // Keeps the number of users that follows the user instance
    private String id; // user's ID
    private String name; // user's name
    private final ArrayList<Post> posts; // posts list
    private final ArrayList<User> followers; // Followers list

    public User() { // constructor to initialize the posts' and follower's list
        posts = new ArrayList<>();
        followers = new ArrayList<>();
    }

    public void addPost(Post post) { // adds a post to the posts' list
        posts.add(post);
    }

    public void addFollower(User user) { // adds a follower to the follower's list
        followers.add(user);
    }

    public void incFollows() { // increment the number of follows of the user
        follows++;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFollows() {
        return follows;
    }

    public ArrayList<User> getFollowers() {
        return followers;
    }

    public String getId() {
        return id;
    }

    public ArrayList<Post> getPosts() {
        return posts;
    }

    public String getName() {
        return name;
    }
}

class Post { // A class that represents a single post

    private String body; // Post's body
    private final ArrayList<String> topics; // A list that holds the topics of each post

    Post() {
        topics = new ArrayList<>();
    } // Initializes the list of topics

    void addTopic(String topic) { // Adds a topic to the list of topics
        topics.add(topic);
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public ArrayList<String> getTopics() {
        return topics;
    }
}