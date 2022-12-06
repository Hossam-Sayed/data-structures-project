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

    //O(n), where n is the number of char in xml file
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
        }
    }

    XML(String s) {
        this.xml = s;
    }

    //O(getErrors) = O(n), Same as order of getErrors
    boolean isValid() {
        if (valid) {
            return true;
        }
        ArrayList<String> errors = getErrors();
        if (errors == null) {
            valid = true;
            return true;
        }
        return false;
    }

    ArrayList<String> getErrors() {
        ArrayList<String> errors = new ArrayList<>();
        Stack<String> opennedTags = new Stack<>();
        boolean inTag = false;
        int line = 1;
        String tag = "";
        char[] xmlchars = xml.toCharArray();
        for (int i = 0; i < xmlchars.length; i++) {
            if (!inTag) {
                switch (xmlchars[i]) {
                    case '\n' -> line++;
                    case '<' -> {
                        inTag = true;
                        tag = "<";
                    }
                }
            } else if (inTag) {
                tag += xmlchars[i];
                if (xmlchars[i] == '>') {
                    if (isOpeningTag(tag)) {
                        opennedTags.push(tag);
                    } else if (isClosingTag(tag) && arePairedTags(opennedTags.peek(), tag)) {
                        opennedTags.pop();
                    } else if (isClosingTag(tag) && !arePairedTags(opennedTags.peek(), tag)) {
                        Stack<String> unclosedtags = new Stack<>();
                        while (!opennedTags.empty() && !arePairedTags(opennedTags.peek(), tag)) {
                            unclosedtags.push(opennedTags.pop());
                        }
                        if (opennedTags.empty()) {
                            errors.add("Line " + line + ": No openning tag for closing tag " + tag);
                            while (!unclosedtags.empty()) {
                                opennedTags.push(unclosedtags.pop());
                            }
                        } else {
                            while (!unclosedtags.empty()) {
                                errors.add("Line " + line + ": Expected closing tag for " + unclosedtags.pop()
                                        + " before closing tag " + tag);
                            }
                            opennedTags.pop();
                        }
                    }
                    inTag = false;
                    tag = "";
                }
            }
        }
        if (!opennedTags.empty()) {
            errors.add("Expected closing tag for " + opennedTags.pop() + " before the end of the file");
        }
        if (errors.isEmpty()) {
            return null;
        } else {
            return errors;
        }
    }

    /*private static int tagLevel(String t) {
        switch (t) {
            case "<users>" , "</users>":
                return 0;
            case "<user>" , "</user>":
                return 1;
            case "<id>" , "</id>" , "<name>" , "</name>" , "<posts>", "</posts>",
                    "<followers>", "</followers>":
                return 2;
            case "<post>","<follower>", "</post>", "</follower>":
                return 3;

        }
    }*/
    
    //O(n), where n is the number of char in xml file
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
            } else { //normal case: add char to the slice string,
                s += xmlchars[i];
            }
        }
        this.sliced = true;
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

    //O(n), where n is the number of slices in slicedXML
    String minifyXML() {
        if (!sliced) {
            sliceXML();
        }
        String minified = "";
        for (String s : slicedXML) {
            minified += s;
        }
        return minified;
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
    //check if passed openning and closing tags match

    private boolean arePairedTags(String openningTag, String closingTag) {
        return removeAngleBrackets(openningTag).equals(closingTag.substring(2, closingTag.length() - 1));
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
        XML xml = new XML(new File("sample with errors.xml"));
        ArrayList<String> errors = xml.getErrors();
        if (errors != null) {
            for (String s : errors) {
                System.out.println(s);
            }
        }else System.out.println("no errors");
    }
}
