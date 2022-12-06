package com.mycompany.datastructuresproject;

import java.util.ArrayList;

public class Graph {

    private ArrayList<User> users;

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
    private ArrayList<Post> posts;
    private ArrayList<User> followers;

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
}

class Post {

    private String body;
    private ArrayList<String> topics;

    public void setBody(String body) {
        this.body = body;
    }

    void addTopic(String topic) {
        topics.add(topic);
    }
}
