package com.mycompany.datastructuresproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class XML {

    private String xml;
    private boolean valid = false;
    private boolean sliced = false;
    private ArrayList<String> slicedXML;
    private Tree xmlTree;
    private Graph xmlGraph;

    XML(File file) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            xml = "";
            while (line != null) {
                if (xml.equals("")) {
                    xml = line;
                } else {
                    xml = xml + "\n" + line;
                }
                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    XML(String s) {
        this.xml = s;
    }

    void sliceXML() {
        boolean inValue = false;
        String currentLeafTag = null;
        slicedXML = new ArrayList<String>();
        char[] xmlchars = xml.toCharArray();
        String s = "";
        for (int i = 0; i < xmlchars.length; i++) {
            if (xmlchars[i] == '\n') { //slice when you find a new line (made to terminate data)
                inValue = false;
                slicedXML.add(s);
                s = "";
            } else if (!inValue && xmlchars[i] == '>') { //slice when you find an ending of a tag
                s += xmlchars[i];
                slicedXML.add(s);
                s = "";
                //skip white spaces after the tag
                while (i + 1 < xmlchars.length
                        && (xmlchars[i + 1] == '\n' || xmlchars[i + 1] == ' ' || xmlchars[i + 1] == '\t')) {
                    i++;
                }
                //check whether next String is value or tag
                if (i + 1 < xmlchars.length && xmlchars[i + 1] != '<') {
                    currentLeafTag = slicedXML.get(slicedXML.size() - 1);
                    inValue = true;
                }
            } else if (!inValue && (xmlchars[i] == ' ' || xmlchars[i] == '\t')) {//Skip white spaces outside values
            } else if (inValue && xmlchars[i + 1] == '<') { //slice before the closing tag
                s += xmlchars[i];
                slicedXML.add(s);
                s = "";
                inValue = false;
            } //these commented else if replaces the above else if, if the XML attribute value can contain <,>.
            /*else if (i + 1 < xmlchars.length
                    && inValue && xmlchars[i] == '<' && xmlchars[i + 1] == '/') {//check if char '<' is inside the data or start of the closing tag
                int j = 2; //2 to skip "</" and start checking the tagname
                //currentLeafTag.charAt(j - 1) the -1 because it doesn't contain '/'
                while (j - 1 < currentLeafTag.length() && currentLeafTag.charAt(j - 1) == xmlchars[i + j]) {
                    j++;
                }
                //if we reached the end if LeafTag then the two tags matches, check for '>' char
                if (j - 1 == currentLeafTag.length()) {
                    slicedXML.add(s);
                    s = "";
                    inValue = false;
                    //decrement i to avoid skipping the '<'
                    i--;
                }else{ //Normal case : add char to the slice string, '<' is inside the data not a start of the tag
                    s += xmlchars[i];
                }
            }*/ else { //normal case: add char to the slice string,
                s += xmlchars[i];
            }
        }
    }
    
    void xmlToTree() {
        if (!valid || !sliced) {
            return;
        }
        Stack<TreeNode> s = new Stack<>();
        for (String item : slicedXML) {
            if (isOpeningTag(item)) { // If it is an open tag
                TreeNode root = new TreeNode(removeAngleBrackets(item));
                if (s.isEmpty()) { // First time to push, We create the Tree object then push <users>
                    xmlTree = new Tree(root);
                }
                s.push(root); // Push open tag to stack
            } else if (isClosingTag(item)) { // If it is a closed tag
                TreeNode child = s.peek();
                s.pop(); // pop closed tag from stack
                if (s.isEmpty()) {
                    continue; // Or exit (finished the arraylist)
                }
                TreeNode parent = s.peek();
                parent.insertChild(child); // Make popped tag child to the tag on the top of stack
            } else if (isTag(item)); else { // If it is data
                s.peek().setData(item);
            }
        }
    }

    void xmlToGraph() {
        if (!valid || !sliced) {
            return;
        }
        xmlGraph = new Graph();
        UserNode user = null;
        Post post = null;
        Stack<String> s = new Stack<>();
        for (String item : slicedXML) {
            if (isOpeningTag(item)) {
                s.push(item);
                switch (item) {
                    case "<user>" ->
                        user = new UserNode();
                    case "<post>" ->
                        post = new Post();
                }
            } else if (isClosingTag(item)) {
                switch (item) {
                    case "</user>" ->
                        xmlGraph.addUser(user); // May set user = null
                    case "</post>" ->
                        user.addPost(post); // May set post = null
                }
                s.pop();
            } else if (isTag(item)); else {
                switch (s.peek()) {
                    case "<id>" -> {
                        String str = s.peek();
                        s.pop();
                        if (s.peek().equals("<follower>")) {
                            user.addFollower(item);
                        } else {
                            user.setId(item);
                        }
                        s.push(str);
                    }
                    case "<name>" ->
                        user.setName(item);
                    case "<body>" ->
                        post.setBody(item);
                    case "<topic>" ->
                        post.addTopic(item);
                }
            }
        }
    }

    private boolean isOpeningTag(String str) {
        return (isTag(str) // It is a tag
                && str.charAt(1) != '/' // Not a closed tag
                && str.charAt(1) != '!' // Not a commnet
                && str.charAt(1) != '?'); // Not a header
    }

    private boolean isClosingTag(String str) {
        return (isTag(str) && str.charAt(1) == '/');
    }

    boolean isTag(String str) {
        return (str.charAt(0) == '<' // Start with <
                && str.charAt(str.length() - 1) == '>'); // End with >
    }

    private String removeAngleBrackets(String str) {
        return str.substring(1, str.length() - 1);
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        XML xml = new XML(new File("sample.xml"));
        xml.sliceXML();
        for (String s : xml.slicedXML) {
            System.out.println(s);
        }
    }
}
