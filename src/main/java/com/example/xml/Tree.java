package com.example.xml;

import java.util.ArrayList;

public class Tree { // A class that represents the tree of the XML file

    private final TreeNode root; // The tree's root element

    Tree(TreeNode root) { // Setting the root of the tree when instantiating it
        this.root = root;
    }

    TreeNode getRoot() {
        return root;
    }
}

class TreeNode { // A class that represents a tag in the XML file

    private final String tagName; // The tag name
    private final ArrayList<TreeNode> children; // The tags children
    private String data; // The tag's data

    public TreeNode(String strTag) { // Initializes the tag name and the list of children
        this.tagName = strTag;
        children = new ArrayList<>();
    }

    void insertChild(TreeNode node) {
        children.add(node);
    } // Inserts a child to the current tag (node)

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
