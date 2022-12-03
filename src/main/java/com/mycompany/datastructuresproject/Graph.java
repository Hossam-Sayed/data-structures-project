package com.mycompany.datastructuresproject;

import java.util.ArrayList;

public class Graph {

    private ArrayList<UserNode> users;

    void addUser(UserNode node) {
        users.add(node);
    }
}

class UserNode {

    private String id;
    private String name;
    private ArrayList<Post> posts;
    private ArrayList<String> followersIDs;

    public UserNode() {
        posts = new ArrayList<>();
        followersIDs = new ArrayList<>();
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

    public void addFollower(String id) {
        followersIDs.add(id);
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
