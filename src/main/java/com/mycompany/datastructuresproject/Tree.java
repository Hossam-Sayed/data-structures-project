package com.mycompany.datastructuresproject;

import java.util.ArrayList;

public class Tree {

    private TreeNode root;

    public Tree(TreeNode root) {
        this.root = root;
    }

    public TreeNode getRoot() {
        return root;
    }
}

class TreeNode {

    private final String tagName;
    private ArrayList<TreeNode> children;
    private String data;

    public TreeNode(String strTag) {
        this.tagName = strTag;
        children = new ArrayList<>();
    }

    void insertChild(TreeNode node) {
        children.add(node);
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTagName() {
        return tagName;
    }

    public ArrayList<TreeNode> getChildren() {
        return children;
    }

    public String getData() {
        return data;
    }

}
