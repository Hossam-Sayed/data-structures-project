package com.example.xml;

import java.util.ArrayList;

public class Graph {

    private final ArrayList<User> users;

    Graph() {
        users = new ArrayList<>();
    }

    void addUser(User node) {
        users.add(node);
    }

    ArrayList<User> getUsers() {
        return users;
    }
}

class User {

    private int follows = 0;
    private String id;
    private String name;
    private final ArrayList<Post> posts;
    private final ArrayList<User> followers;

    public User() {
        posts = new ArrayList<>();
        followers = new ArrayList<>();
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addPost(Post post) {
        posts.add(post);
    }

    public void addFollower(User user) {
        followers.add(user);
    }

    public void incFollows() {
        follows++;
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

class Post {

    private String body;
    private final ArrayList<String> topics;

    Post() {
        topics = new ArrayList<>();
    }

    public void setBody(String body) {
        this.body = body;
    }

    void addTopic(String topic) {
        topics.add(topic);
    }

    public String getBody() {
        return body;
    }

    public ArrayList<String> getTopics() {
        return topics;
    }
}