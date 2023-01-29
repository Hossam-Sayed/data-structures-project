package com.example.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class XML {

    int min, max = 0;

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    private boolean valid = false;
    private boolean sliced = false;
    private String xml;
    private Tree xmlTree;
    private Graph xmlGraph;
    private ArrayList<String> slicedXML;

    //O(n), where n is the number of char in xml file
    XML(File file) {
        BufferedReader reader;
        StringBuilder xml = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                xml.append(line);
                xml.append("\n");
                line = reader.readLine();
            }
            xml.deleteCharAt(xml.length() - 1);
            this.xml = xml.toString();
            reader.close();
        } catch (IOException ignored) {
        }
    }

    XML(String s) {
        this.xml = s;
    }

    public String getXml() {
        return xml;
    }

    void compress(String path) {
        Compression.compress(this.xml, path);
    }

    static String decompress(String path) {
        return Compression.decompress(new File(path));
    }

    void fixErrors() {
        getErrors(true);
    }

    boolean isValidold() {
        if (valid) {
            return true;
        }
        ArrayList<String> errors = getErrors(false);
        valid = errors == null ? true : valid;
        return errors == null;
    }

    public boolean isValid() {
        return valid;
    }

    ArrayList<String> getErrors(boolean fix) {
        ArrayList<String> errors = new ArrayList<>();
        Stack<String> opennedTags = new Stack<>();
        StringBuilder fixedXML = fix ? new StringBuilder(xml.length() * 2) : null;
        int addedchar = 0;
        boolean inTag = false;
        boolean hasValue = false;
        int line = 1;
        String tag = "";
        char[] xmlchars = xml.toCharArray();
        for (int i = 0; i < xmlchars.length; i++) {
            if (!inTag) {
                switch (xmlchars[i]) {
                    case '\n' -> {
                        line++;
                        if (fix) {
                            fixedXML.append(xmlchars[i]);
                        }
                    }
                    case '<' -> {
                        inTag = true;
                        tag = "<";
                    }
                    default -> {
                        if (!(xmlchars[i] == ' ' || xmlchars[i] == '\t')) {
                            if (opennedTags.empty()) {
                                errors.add("Line " + line + ": Attribute value with no opened tags");
                            } else {
                                hasValue = true;
                                while (i < xmlchars.length && !(xmlchars[i] == '\n' || xmlchars[i] == '<')) {
                                    if (fix) {
                                        fixedXML.append(xmlchars[i]);
                                    }
                                    i++;
                                }
                                i--;
                            }
                        } else {
                            if (fix) {
                                fixedXML.append(xmlchars[i]);
                            }
                        }
                    }
                }
            } else if (inTag) {
                tag += xmlchars[i];
                if (xmlchars[i] == '>') {
                    if (isOpeningTag(tag)) {
                        if (!opennedTags.empty() && hasValue) {
                            errors.add("Line " + line + ": Tag " + opennedTags.peek()
                                    + " must be closed before openning " + tag
                                    + " tag, as " + opennedTags.peek() + " has attribute value");
                            if (fix) {
                                fixedXML.append("</");
                                fixedXML.append(opennedTags.peek().substring(1));
                            }
                            opennedTags.pop();
                            hasValue = false;
                        }
                        opennedTags.push(tag);
                        if (fix) {
                            fixedXML.append(tag);
                        }
                    } else if (isClosingTag(tag)) {
                        if (!opennedTags.empty() && arePairedTags(opennedTags.peek(), tag)) {
                            opennedTags.pop();
                            hasValue = false;
                            if (fix) {
                                fixedXML.append(tag);
                            }
                        } else if (!opennedTags.empty() && !arePairedTags(opennedTags.peek(), tag)) {
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
                                    errors.add("Line " + line + ": Expected closing tag for " + unclosedtags.peek()
                                            + " before closing tag " + tag);
                                    if (fix) {
                                        fixedXML.append("</");
                                        fixedXML.append(unclosedtags.peek().substring(1));
                                    }
                                    unclosedtags.pop();
                                }
                                opennedTags.pop();
                                if (fix) {
                                    fixedXML.append(tag);
                                }
                            }
                        }
                    }
                    inTag = false;
                    tag = "";
                }
            }
        }
        while (!opennedTags.empty()) {
            errors.add("Expected closing tag for " + opennedTags.peek() + " before the end of the file");
            if (fix) {
                fixedXML.append("</");
                fixedXML.append(opennedTags.peek().substring(1));
            }
            opennedTags.pop();
        }
        if (fix) {
            this.xml = fixedXML.toString();
        }
        return errors.isEmpty() ? null : errors;
    }

    //O(n), where n is the number of char in xml file
    void sliceXML() {
        boolean inValue = false;
        String currentLeafTag = null;
        slicedXML = new ArrayList<String>();
        char[] xmlchars = xml.toCharArray();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < xmlchars.length; i++) {
            if (xmlchars[i] == '\n') { //slice when you find a new line (made to terminate data)
                inValue = false;
                slicedXML.add(s.toString());
                s = new StringBuilder();
            } else if (!inValue && xmlchars[i] == '>') { //slice when you find an ending of a tag
                s.append(xmlchars[i]);
                slicedXML.add(s.toString());
                s = new StringBuilder();
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
                s.append(xmlchars[i]);
                slicedXML.add(s.toString());
                s = new StringBuilder();
                inValue = false;
            } else { //normal case: add char to the slice string,
                s.append(xmlchars[i]);
            }
        }
        this.sliced = true;
    }

    //O(n), where n is the number of slices in slicedXML
    String minifyXML() {
        if (!sliced) {
            sliceXML();
        }
        StringBuilder minified = new StringBuilder();
        for (String s : slicedXML) {
            minified.append(s);
        }
        return minified.toString();
    }

    // O(n), n is the length of the XML file
    void xmlToTree() {
        if (!valid) {
            return;
        } else if (!sliced) {
            sliceXML();
        }
        Stack<TreeNode> s = new Stack<>();
        for (String item : slicedXML) {
            if (item == null || item.equals("")) ;
            else if (isOpeningTag(item)) { // If it is an open tag
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
            } else if (isTag(item)) ;
            else { // If it is data
                s.peek().setData(item);
            }
        }
    }

    // O(n), n is the length of the XML file
    void xmlToGraph() {
        if (!valid || !sliced) {
            return;
        }
        setMaxAndMinIds();
        xmlGraph = new Graph();
        User user = null;
        Post post = null;
        User dummy[] = new User[max - min + 1];
        Stack<String> s = new Stack<>();
        for (String item : slicedXML) {
            if (isOpeningTag(item)) {
                s.push(item);
                if (item.equals("<post>")) {
                    post = new Post();
                }
            } else if (isClosingTag(item)) {
                switch (item) {
                    case "</user>" -> xmlGraph.addUser(user); // May set user = null
                    case "</post>" -> user.addPost(post); // May set post = null
                }
                s.pop();
            } else if (isTag(item)) ;
            else {
                switch (s.peek()) {
                    case "<id>" -> {
                        int index = Integer.parseInt(item) - min; // Calculate index to access dummy
                        s.pop(); // Pop ID tag from stack top
                        if (s.peek().equals("<follower>")) {  // Current ID belogs to a follower
                            if (dummy[index] == null) { // If we haven't met this follower before
                                dummy[index] = new User(); // Add this follower to dummy list
                            }
                            dummy[index].incFollows(); // Increment the follower's follows by one
                            user.addFollower(dummy[index]); // Add this follower to followers list of current user
                        } else { // Current ID belogs to a user
                            if (dummy[index] == null) { // If we haven't met this user before
                                user = new User(); // Set this user as current user
                                dummy[index] = user; // Add this user to dummy list
                            } else { // We mit this user before
                                user = dummy[index]; // Set this user as current user from dummy list
                            }
                            user.setId(item); // Set the ID of the current user
                        }
                        s.push("<id>"); // Push the ID tag again
                    }
                    case "<name>" -> user.setName(item); // Set the name of the current user
                    case "<body>" -> post.setBody(item); // Set the post body to Post object
                    case "<topic>" -> post.addTopic(item); // Add the post topic to Post object topics list
                }
            }
        }
    }

    String formatingNode(String str, TreeNode node) {
        String addition = "";
        addition += str;
        addition += "\t";
        String strs = "";
        strs += "<" + (node.getTagName()) + ">";
        ArrayList<TreeNode> childrens = node.getChildren();
        if (childrens != null) {
            for (int i = 0; i < childrens.size(); i++) {
                strs += addition;
                strs += formatingNode(addition, childrens.get(i));
            }
        } else {
            strs += (addition);
            strs += (node.getData());
            strs += str;
            strs += "</";
            strs += (node.getTagName());
            strs += ">";
            return strs;
        }
        strs += str;
        strs += "</";
        strs += (node.getTagName());
        strs += ">";
        return strs;
    }

    void format() {
        if (xmlTree == null) {
            this.xmlToTree();
        }
        String str = "";
        TreeNode node = xmlTree.getRoot();
        if (node != null) {
            str = formatingNode("\n", node);
        }
        xml = str;
    }

    void stringToXmlFile(String path) {
        // create a file object for the current location
        File file = new File(path);
        try {
            // create a new file with name specified
            // by the file object
            boolean value = file.createNewFile();
            if (value) {
                //System.out.println("New Java File is created.");
            } else {
                //System.out.println("The file already exists.");
            }
            try (FileWriter output = new FileWriter(path)) {
                output.write(xml);
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    void str_to_jsonFile(String json) {
        // create a file object for the current location
        File file = new File("exportedjson.json");
        try {
            // create a new file with name specified
            // by the file object
            boolean value = file.createNewFile();
            if (value) {
                //    System.out.println("New Java File is created.");
            } else {
                //    System.out.println("The file already exists.");
            }
            try (FileWriter output = new FileWriter("exportedjson.json")) {
                output.write(json);
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    String jsonFormatingNode(String str, TreeNode node) {
        String addition = "";
        addition += str;
        addition += "\t";
        String strs = "";
        String type = "";
        ArrayList<TreeNode> childrens = node.getChildren();
        if (childrens != null) {
            if (childrens.get(0).getData() != null) {
                type = "{";
            } else {
                type = "[";
            }
            strs += ("\t\"" + (node.getTagName()) + "\":" + type);
            for (int i = 0; i < childrens.size(); i++) {
                strs += addition;
                strs += jsonFormatingNode(addition, childrens.get(i));
            }
        } else {
            strs += "\t\"" + (node.getTagName()) + "\":";
            //strs += (addition + "\t");
            strs += (" \"" + node.getData() + "\"");
            //strs += str;
            //strs += "\t";
            strs += ",";
            return strs;
        }
        strs += str;
        strs += "\t";
        if (type == "{") {
            strs += "}";
        } else {
            strs += "]";
        }
        return strs;
    }

    String xmlToJson() {
        if (xmlTree == null) {
            this.xmlToTree();
            //  xmlToJson();
        }
        String str = "{\n";
        TreeNode node = xmlTree.getRoot();
        if (node != null) {
            str += jsonFormatingNode("\n", node);
        }
        str += "\n}";
        return str;
    }

    // O(n), n is the number of users
    User getMostActive() {
        User mostActive = xmlGraph.getUsers().get(0);
        int maxFollows = mostActive.getFollows() + mostActive.getFollowers().size();
        for (User user : xmlGraph.getUsers()) {
            if (user.getFollows() + user.getFollowers().size() > maxFollows) {
                maxFollows = user.getFollows() + user.getFollowers().size();
                mostActive = user;
            }
        }
        return mostActive;
    }

    // O(n), n is the number of users
    User getMostInfluencer() {
        User mostInfluencer = xmlGraph.getUsers().get(0);
        int maxFollowers = mostInfluencer.getFollowers().size();
        for (User user : xmlGraph.getUsers()) {
            if (user.getFollowers().size() > maxFollowers) {
                maxFollowers = user.getFollowers().size();
                mostInfluencer = user;
            }
        }
        return mostInfluencer;
    }

    // O(n), n is the number of users in the graph
    public ArrayList<User> getMutualFollowers(String id1, String id2) {
        ArrayList<User> mutualFollowers = new ArrayList<>();
        User dummy[] = new User[max - min + 1];
        for (User user : xmlGraph.getUsers()) {
            if (user.getId().equals(id1)) {
                for (User follower : user.getFollowers()) {
                    int index = Integer.parseInt(follower.getId()) - min;
                    dummy[index] = follower;
                }
                break;
            }
        }
        for (User user : xmlGraph.getUsers()) {
            if (user.getId().equals(id2)) {
                for (User follower : user.getFollowers()) {
                    int index = Integer.parseInt(follower.getId()) - min;
                    if (dummy[index] != null) {
                        mutualFollowers.add(dummy[index]);
                    }
                }
                break;
            }
        }

        return (mutualFollowers.isEmpty()) ? null : mutualFollowers;
    }

    // O(n), n is the maximum number of followers for any user (number of users in the graph - 1)
    public ArrayList<User> getMutualFollowers(User user1, User user2) {
        ArrayList<User> mutualFollowers = new ArrayList<>();
        User dummy[] = new User[max - min + 1];
        for (User follower : user1.getFollowers()) {
            int index = Integer.parseInt(follower.getId()) - min;
            dummy[index] = follower;
        }
        for (User follower : user2.getFollowers()) {
            int index = Integer.parseInt(follower.getId()) - min;
            if (dummy[index] != null) {
                mutualFollowers.add(dummy[index]);
            }
        }
        return (mutualFollowers.isEmpty()) ? null : mutualFollowers;
    }

    enum Relativity {DISTANT, FOF, FOLLOWER, ME}

    public ArrayList<User> suggestFollowers(User user) {
        ArrayList<User> suggestedList = new ArrayList<>();
        int dummy[] = new int[max - min + 1];
        Relativity[] relatives = new Relativity[max - min + 1];
        relatives[Integer.parseInt(user.getId()) - min] = Relativity.ME;
        for (User follower : user.getFollowers()) {
            int followerIndex = Integer.parseInt(follower.getId()) - min;
            relatives[followerIndex] = Relativity.FOLLOWER;
            for (User distantFollower : follower.getFollowers()) {
                int index = Integer.parseInt(distantFollower.getId()) - min;
                if (relatives[index] == Relativity.ME || relatives[index] == Relativity.FOLLOWER) {
                    continue;
                } else {
                    if (dummy[index] == 0) {
                        dummy[index]++;
                        suggestedList.add(distantFollower);
                    }
                }
            }
        }
        return (suggestedList.isEmpty()) ? null : suggestedList;
    }

    public ArrayList<User> suggestFollowers(String id) {
        User user = null;
        for (User user1 : xmlGraph.getUsers()) {
            if (user1.getId().equals(id)) {
                user = user1;
                break;
            }
        }
        ArrayList<User> suggestedList = new ArrayList<>();
        int dummy[] = new int[max - min + 1];
        Relativity[] relatives = new Relativity[max - min + 1];
        relatives[Integer.parseInt(user.getId()) - min] = Relativity.ME;
        for (User follower : user.getFollowers()) {
            int followerIndex = Integer.parseInt(follower.getId()) - min;
            relatives[followerIndex] = Relativity.FOLLOWER;
        }
        for (User follower : user.getFollowers()) {
            for (User distantFollower : follower.getFollowers()) {
                int index = Integer.parseInt(distantFollower.getId()) - min;
                if (relatives[index] == Relativity.ME || relatives[index] == Relativity.FOLLOWER) {
                    continue;
                } else {
                    if (dummy[index] == 0) {
                        dummy[index]++;
                        suggestedList.add(distantFollower);
                    }
                }
            }
        }
        return (suggestedList.isEmpty()) ? null : suggestedList;
    }

    public ArrayList<Post> searchPosts(String searchWord) {
        searchWord = searchWord.toLowerCase();
        ArrayList<Post> allPosts = new ArrayList<>();
        ArrayList<Post> searchedPosts = new ArrayList<>();
        boolean found;

        for (User user : xmlGraph.getUsers()) {
            allPosts.addAll(user.getPosts());
        }

        for (Post post : allPosts) {
            found = false;
            String[] words = post.getBody().split(" ");

            for (String word : words) {
                if (word.toLowerCase().equals(searchWord)) {
                    searchedPosts.add(post);
                    found = true;
                    break;
                }
            }

            if (!found) {
                for (String topic : post.getTopics()) {
                    if (topic.toLowerCase().equals(searchWord)) {
                        searchedPosts.add(post);
                        break;
                    }
                }
            }
        }

        return (searchedPosts.isEmpty()) ? null : searchedPosts;
    }

    // O(n), n is the length of the XML file
    void setMaxAndMinIds() {
        for (int i = 0; i < slicedXML.size(); i++) {
            if (slicedXML.get(i).equals("<id>")) {
                int val = Integer.parseInt(slicedXML.get(i + 1));
                min = Math.min(val, min);
                max = Math.max(val, max);
            }
        }
    }

    //check if passed openning and closing tags match
    private boolean arePairedTags(String openningTag, String closingTag) {
        return removeAngleBrackets(openningTag).equals(closingTag.substring(2, closingTag.length() - 1));
    }

    private boolean isOpeningTag(String str) {
        return (isTag(str) // It is a tag^
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

//        suggestFollowers test
//        XML xml = new XML(new File("sampleFollowers.xml"));
//        if (xml.isValid()) {
//            xml.sliceXML();
//            xml.xmlToGraph();
//        }
//        String id = "1";
//        ArrayList<User> users = xml.suggestFollowers(id);
//        System.out.println("User " + id);
//        if (users == null) {
//            System.out.println("No FOFs");
//            return;
//        }
//        for (User user : users) {
//            System.out.println("FOF " + user.getId());
//        }

        //compression test
        /*XML xml = new XML(new File("sample.xml"));
        xml.compress("File1");
        xml = new XML(new File("sample with errors.xml"));
        xml.compress("File2");
        System.out.println(xml.decompress("File1"));
        String s = xml.decompress("File2");
        System.out.println(s);*/
        //System.out.println(xml.xml);
//        ArrayList<String> errors = xml.getErrors(true);
//        if (errors == null) {
//            System.out.println("no errors");
//        } else {
//            for (String s : errors) {
//                System.out.println(s);
//            }
//        }

        /*if (xml.isValid()) {
            xml.sliceXML();
            xml.format();
            System.out.println(xml.xml);
            //String ste = xml.xmlToJson();
            //System.out.println(ste);
            // xml.str_to_xmlFile();
            //xml.str_to_jsonFile(ste);
        }*/
    }
}
