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

    int min, max = 0; // Min and max indices that correspond to min and max Users IDs

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    private boolean valid = false;
    private boolean sliced = false;
    private String xml;
    private Tree xmlTree; // Tree representation of the XML file
    private Graph xmlGraph; // Graph representation of the XML file
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
        } catch (IOException e) {
            // Ignored as we use file chooser in GUI so no possible errors will occur
            e.getStackTrace();
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

    boolean isValidOld() {
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
        Stack<String> openedTags = new Stack<>();
        StringBuilder fixedXML = fix ? new StringBuilder(xml.length() * 2) : null;
        int addedChar = 0;
        boolean inTag = false;
        boolean hasValue = false;
        int line = 1;
        String tag = "";
        char[] xmlChars = xml.toCharArray();
        for (int i = 0; i < xmlChars.length; i++) {
            if (!inTag) {
                switch (xmlChars[i]) {
                    case '\n' -> {
                        line++;
                        if (fix) {
                            fixedXML.append(xmlChars[i]);
                        }
                    }
                    case '<' -> {
                        inTag = true;
                        tag = "<";
                    }
                    default -> {
                        if (!(xmlChars[i] == ' ' || xmlChars[i] == '\t')) {
                            if (openedTags.empty()) {
                                errors.add("Line " + line + ": Attribute value with no opened tags");
                                while (i < xmlChars.length && xmlChars[i] != '\n' && xmlChars[i] != '<') {
                                    i++;
                                }
                            } else {
                                hasValue = true;
                                while (i < xmlChars.length && !(xmlChars[i] == '\n' || xmlChars[i] == '<')) {
                                    if (fix) {
                                        fixedXML.append(xmlChars[i]);
                                    }
                                    i++;
                                }
                            }
                            i--;
                        } else {
                            if (fix) {
                                fixedXML.append(xmlChars[i]);
                            }
                        }
                    }
                }
            } else if (inTag) {
                tag += xmlChars[i];
                if (xmlChars[i] == '>') {
                    if (isOpeningTag(tag)) {
                        if (!openedTags.empty() && hasValue) {
                            errors.add("Line " + line + ": Tag " + openedTags.peek()
                                    + " must be closed before opening " + tag
                                    + " tag, as " + openedTags.peek() + " has attribute value");
                            if (fix) {
                                fixedXML.append("</");
                                fixedXML.append(openedTags.peek().substring(1));
                            }
                            openedTags.pop();
                            hasValue = false;
                        }
                        openedTags.push(tag);
                        if (fix) {
                            fixedXML.append(tag);
                        }
                    } else if (isClosingTag(tag)) {
                        if (!openedTags.empty() && arePairedTags(openedTags.peek(), tag)) {
                            openedTags.pop();
                            hasValue = false;
                            if (fix) {
                                fixedXML.append(tag);
                            }
                        } else if (!openedTags.empty() && !arePairedTags(openedTags.peek(), tag)) {
                            Stack<String> unclosedtags = new Stack<>();
                            while (!openedTags.empty() && !arePairedTags(openedTags.peek(), tag)) {
                                unclosedtags.push(openedTags.pop());
                            }
                            if (openedTags.empty()) {
                                errors.add("Line " + line + ": No opening tag for closing tag " + tag);
                                while (!unclosedtags.empty()) {
                                    openedTags.push(unclosedtags.pop());
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
                                openedTags.pop();
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
        while (!openedTags.empty()) {
            errors.add("Expected closing tag for " + openedTags.peek() + " before the end of the file");
            if (fix) {
                fixedXML.append("</");
                fixedXML.append(openedTags.peek().substring(1));
            }
            openedTags.pop();
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

    //     O(n), n is the length of the XML file
    void xmlToTree() {
        if (!valid) { // If the XML is not valid, return
            return;
        } else if (!sliced) { // If the XML is not sliced
            sliceXML(); // Slice it
        }
        Stack<TreeNode> s = new Stack<>(); // Instantiate a stack object
        for (String item : slicedXML) { // Iterate over words in the XML file
            if (item == null || item.equals("")) ; // if a word in the XML file is null or empty, skip it
            else if (isOpeningTag(item)) { // If it is an opening tag
                TreeNode root = new TreeNode(removeAngleBrackets(item)); // Instantiate a tree node and set its tag name
                if (s.isEmpty()) { // If it's the first time to push, create the Tree object then push <users>
                    xmlTree = new Tree(root); // Instantiate the tree object
                }
                s.push(root); // Push opening tag to stack
            } else if (isClosingTag(item)) { // If it is a closing tag
                TreeNode child = s.peek(); // Get the stack top
                s.pop(); // Pop closing tag from stack
                if (s.isEmpty()) {
                    continue; // Or exit (finished XML words)
                }
                TreeNode parent = s.peek(); // Get the stack top again
                parent.insertChild(child); // Make popped tag child to the tag on the stack top
            } else if (isTag(item))
                ; // If it is a tag (not data) but not opening or closing tags then it is either an XML header or comment, so skip it
            else { // If it is data
                s.peek().setData(item);
            }
        }
    }

    //    O(n), n is the length of the XML file
    void xmlToGraph() {
        if (!valid) { // If the XML is not valid, return
            return;
        } else if (!sliced) { // If the XML is not sliced
            sliceXML(); // Slice it
        }
        setMaxAndMinIds(); // Set the max and min indices
        xmlGraph = new Graph(); // Instantiate a class object
        User user = null; // Current user
        Post post = null; // Current post
        User[] idToUserMap = new User[max - min + 1]; // An array that maps the user's ID to the user object in O(1)
        Stack<String> s = new Stack<>(); // Instantiate a stack object
        for (String item : slicedXML) { // Iterate over words in the XML file
            if (isOpeningTag(item)) { // If it is an opening tag
                s.push(item); // Push it to the stack
                if (item.equals("<post>")) { // If the opening tag is <post> tag
                    post = new Post(); // Instantiate a post object to be filled later with body and topics
                }
            } else if (isClosingTag(item)) { // If it is a closing tag
                switch (item) {
                    case "</user>" ->
                            xmlGraph.addUser(user); // If the closing tag is </user> tag, add the current user to the graph
                    case "</post>" ->
                            user.addPost(post); // If the closing tag is </post> tag, add the current post to the current user
                }
                s.pop(); // Pop the stack top (the opening tag that corresponds to the found closing tag)
            } else if (isTag(item))
                ; // If it is a tag (not data) but not opening or closing tags then it is either an XML header or comment, so skip it
            else {
                switch (s.peek()) { // Check the stack top
                    case "<id>" -> { // If the stack top is an <id> tag
                        int index = Integer.parseInt(item) - min; // Calculate index to access idToUserMap
                        s.pop(); // Pop ID tag from stack top
                        if (s.peek().equals("<follower>")) {  // Current ID belongs to a follower
                            if (idToUserMap[index] == null) { // If we haven't met this follower before
                                idToUserMap[index] = new User(); // Add this follower to idToUserMap list
                            }
                            idToUserMap[index].incFollows(); // Increment the follower's follows by one
                            user.addFollower(idToUserMap[index]); // Add this follower to followers list of current user
                        } else { // Current ID belongs to a user
                            if (idToUserMap[index] == null) { // If we haven't met this user before
                                user = new User(); // Set this user as current user
                                idToUserMap[index] = user; // Add this user to idToUserMap
                            } else { // We mit this user before
                                user = idToUserMap[index]; // Set this user as current user from idToUserMap list
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
        StringBuilder addition = new StringBuilder();
        addition.append(str + "\t");
        StringBuilder strs = new StringBuilder();
        strs.append("<" + node.getTagName() + ">");
        ArrayList<TreeNode> childrens = node.getChildren();
        if (!childrens.isEmpty()) {
            for (int i = 0; i < childrens.size(); i++) {
                strs.append(addition);
                strs.append(formatingNode(addition.toString(), childrens.get(i)));
            }
        } else {
            strs.append(addition + node.getData() + str + "</" + node.getTagName() + ">");
            return strs.toString();
        }
        strs.append(str + "</" + node.getTagName() + ">");
        return strs.toString();
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


    String jsonFormattingNode(String str, TreeNode node) {
        StringBuilder addition = new StringBuilder();
        addition.append(str);
        if (!addition.toString().equals(" ")) {
            addition.append("\t");
        }
        StringBuilder strs = new StringBuilder();
        ArrayList<TreeNode> childrens = node.getChildren();
        ArrayList<TreeNode> sortedChildrens = new ArrayList<>();
        int[] visitedChildrens = new int[childrens.size()];

        for (int i = 0; i < childrens.size(); i++) {
            if (visitedChildrens[i] == 1)
                continue;

            sortedChildrens.add(childrens.get(i));
            visitedChildrens[i] = 1;

            for (int j = 0; j < childrens.size(); j++) {
                if (childrens.get(i).getTagName().equals(childrens.get(j).getTagName()) && (i != j)) {
                    sortedChildrens.add(childrens.get(j));
                    visitedChildrens[j] = 1;
                }

            }
        }

        if (!sortedChildrens.isEmpty()) {
            for (int i = 0; i < sortedChildrens.size(); i++) {
                if (i < sortedChildrens.size() - 1 && sortedChildrens.get(i).getTagName().equals(sortedChildrens.get(i + 1).getTagName())) {
                    strs.append(addition + "\"" + (sortedChildrens.get(i).getTagName()) + "\":" + "[");
                    String s = sortedChildrens.get(i).getTagName();
                    while (i < sortedChildrens.size() && sortedChildrens.get(i).getTagName().equals(s)) {
                        if (sortedChildrens.get(i).getChildren() != null && sortedChildrens.get(i).getChildren().size() >= 1) {
                            strs.append(addition + "\t{");
                            strs.append(jsonFormattingNode(addition.toString() + "\t", sortedChildrens.get(i)));
                            if (strs.charAt(strs.length() - 1) == ',') {
                                strs.deleteCharAt(strs.length() - 1);
                            }
                            strs.append(addition + "\t},");
                        } else {
                            strs.append(jsonFormattingNode(addition.toString(), sortedChildrens.get(i)));
                        }
                        i++;
                    }
                    i--;
                    if (strs.charAt(strs.length() - 1) == ',') {
                        strs.deleteCharAt(strs.length() - 1);
                    }
                    strs.append(addition + "],");
                } else {
                    if (sortedChildrens.get(i).getChildren() != null
                            && !sortedChildrens.get(i).getChildren().isEmpty()) {
                        strs.append(addition + "\"" + sortedChildrens.get(i).getTagName() + "\":" + "{");
                        strs.append(jsonFormattingNode(addition.toString(), sortedChildrens.get(i)));
                        if (strs.charAt(strs.length() - 1) == ',') {
                            strs.deleteCharAt(strs.length() - 1);
                        }
                        strs.append(addition + "},");
                    } else {
                        strs.append(addition + "\"" + sortedChildrens.get(i).getTagName() + "\":");
                        strs.append(jsonFormattingNode(" ", sortedChildrens.get(i)));
                    }
                }


            }

        } else {
            strs.append(addition + "\"" + node.getData() + "\"" + ",");
        }
        return strs.toString();

    }

    String xmlToJson() {
        if (xmlTree == null) {
            this.xmlToTree();
            //  xmlToJson();
        }
        StringBuilder str = new StringBuilder();
        str.append("{\n");
        //String str = "{\n";
        TreeNode node = xmlTree.getRoot();
        if (node != null) {
            str.append("\t" + "\"" + node.getTagName() + "\"" + ":{");
            //str +="\t" +"\""+ node.getTagName() +"\""+":{";
            str.append(jsonFormattingNode("\n\t", node));
            //str += jsonFormatingNode("\n\t", node);

        }
        if (str.charAt(str.length() - 1) == ',') {
            str.deleteCharAt(str.length() - 1);
        }
        str.append("\n\t}" + "\n}");
        //str +="\n\t}";
        //str += "\n}";
        return str.toString();
    }

    //    O(n), n is the number of users
    User getMostActive() { // returns the most active user (first user with most followers + follows)
        User mostActive = xmlGraph.getUsers().get(0); // Initialize the most active user as the first user
        int maxActivityDegree = mostActive.getFollows() + mostActive.getFollowers().size(); // Initialize the max degree
        for (User user : xmlGraph.getUsers()) { // Iterate over each user in the graph
            if (user.getFollows() + user.getFollowers().size() > maxActivityDegree) { // If the user has a degree more than the max degree so far
                maxActivityDegree = user.getFollows() + user.getFollowers().size(); // Update maxActivityDegree
                mostActive = user; // Make this user the most active
            }
        }
        return mostActive; // Return the most active user
    }

    //    O(n), n is the number of users
    User getMostInfluencer() { // Returns the most active user (first user with most followers)
        User mostInfluencer = xmlGraph.getUsers().get(0); // Initialize the most active user as the first user
        int maxFollowers = mostInfluencer.getFollowers().size(); // Initialize the max followers
        for (User user : xmlGraph.getUsers()) { // Iterate over each user in the graph
            if (user.getFollowers().size() > maxFollowers) { // If the user has followers more than the max followers so far
                maxFollowers = user.getFollowers().size(); // Update maxFollowers
                mostInfluencer = user; // Make this user the most influencer
            }
        }
        return mostInfluencer; // Return the most influencer user
    }

    //    O(n), n is the max number of followers of a user
    ArrayList<User> getMutualFollowers(String id1, String id2) { // Returns the mutual users between two users
        try { // A try block so that any exception (invalid input parameters or no mutual followers) is handled
            ArrayList<User> mutualFollowers = new ArrayList<>(); // A list to hold the mutual followers objects
            User[] followersArray = new User[max - min + 1]; // An array to hold the followers of the first user
            ArrayList<User> users = xmlGraph.getUsers(); // List of users in the graph

            int i;
            for (i = 0; i < users.size(); i++) {
                if (users.get(i).getId().equals(id1)) { // Iterate over users until the user with the corresponding ID is found
                    for (User follower : users.get(i).getFollowers()) { // Iterate over the followers of the first user
                        int index = Integer.parseInt(follower.getId()) - min; // Calculate the index to access the array
                        followersArray[index] = follower; // Add this follower to the array in the place corresponds to its ID
                    }
                    break;
                }
            }

            if (i == users.size()) { // If the first user is not found
                return null;
            }

            for (i = 0; i < users.size(); i++) {
                if (users.get(i).getId().equals(id2)) { // Iterate over users until the user with the corresponding ID is found
                    for (User follower : users.get(i).getFollowers()) { // Iterate over the followers of the second user
                        int index = Integer.parseInt(follower.getId()) - min; // Calculate the index to access the array
                        if (followersArray[index] != null) { // If this follower is found in the array (was also a follower of the first user)
                            mutualFollowers.add(followersArray[index]); // Add this follower to the mutual followers list
                        }
                    }
                    break;
                }
            }

            if (i == users.size()) { // If the second user is not found
                return null;
            }

            return mutualFollowers; // Return the mutual followers list
        } catch (Exception e) {
            return null; // If the input parameters are invalid or there is no mutual followers, return null
        }
    }

    enum Relativity {FOLLOWER, ME} // Enumerate to determine the relativity degree of a user to another

    //    O(n^2), n is the number of users in the graph.
    ArrayList<User> suggestFollowers(String id) {
        try { // A try block so that any exception (invalid input parameters or no suggested followers) is handled
            User user = null;
            for (User graphUser : xmlGraph.getUsers()) { // Iterate over users until the user with the corresponding ID is found
                if (graphUser.getId().equals(id)) {
                    user = graphUser;
                    break;
                }
            }
            ArrayList<User> suggestedUsers = new ArrayList<>(); // A list to hold the suggested followers objects
            int[] distantFollowersArray = new int[max - min + 1];
            Relativity[] relatives = new Relativity[max - min + 1]; // An array that holds the relativity degree
//            if (Integer.parseInt(id) > max || Integer.parseInt(id) < min) return null; // If the ID parameter is not an ID of a user in the graph, return null
            relatives[Integer.parseInt(user.getId()) - min] = Relativity.ME; // Mark the current user as ME
            for (User follower : user.getFollowers()) { // Iterate over the followers of the user and mark them as Followers
                int followerIndex = Integer.parseInt(follower.getId()) - min;
                relatives[followerIndex] = Relativity.FOLLOWER;
            }
            for (User follower : user.getFollowers()) { // Iterate over each follower of the user
                for (User distantFollower : follower.getFollowers()) { // Iterate over each follower of the follower
                    int index = Integer.parseInt(distantFollower.getId()) - min; // Calculate the index to access the array
                    // If the distant follower is neither a follower to the user nor the user himself, and he wasn't seen before
                    if (relatives[index] != Relativity.ME && relatives[index] != Relativity.FOLLOWER && distantFollowersArray[index] == 0) {
                        distantFollowersArray[index]++; // Mark him as seen
                        suggestedUsers.add(distantFollower); // Add this distant follower to the suggestedUsers
                    }
                }
            }
            return suggestedUsers; // Return the suggestedUsers list
        } catch (Exception e) {
            return null; // If the input parameter is invalid or there is no suggested users, return null
        }
    }


    //    O(n^2), n is the number of words in posts.
    ArrayList<Post> searchPosts(String searchWord) { // Searches all posts for a given word
        searchWord = searchWord.toLowerCase(); // lower casing the given word
        ArrayList<Post> allPosts = new ArrayList<>(); // Initializes a list to hold all posts
        ArrayList<Post> searchedPosts = new ArrayList<>(); // Initializes a list to hold the posts that contain the given word
        boolean found; // To mark a post as found

        for (User user : xmlGraph.getUsers()) { // Iterate over graph users to add all posts of each user to allPosts list
            allPosts.addAll(user.getPosts());
        }

        for (Post post : allPosts) {
            found = false; // Mark the post as not found (at the beginning)
            String[] words = post.getBody().split(" "); // Splitting the post into a words array

            for (String word : words) { // Iterate over each word in the post
                if (word.toLowerCase().equals(searchWord)) { // If the word in the post is the same as the given word
                    searchedPosts.add(post); // Add this post to the searchedPosts list
                    found = true; // Mark the post as found
                    break; // Exit the loop as we found the post
                }
            }

            if (!found) { // If the post is still not found by searching its body, we search its topics
                for (String topic : post.getTopics()) { // Iterate over each topic in the post's topics list
                    if (topic.toLowerCase().equals(searchWord)) { // If the topic is the same as the given word
                        searchedPosts.add(post); // Add this post to the searchedPosts list
                        break; // Exit the loop as we found the post
                    }
                }
            }
        }

        return searchedPosts; // Return the searched posts
    }

    //    O(n), n is the length of the XML file
    void setMaxAndMinIds() { // Sets the minimum and maximum indices of the array
        for (int i = 0; i < slicedXML.size(); i++) { // Iterate over every word in the XML file
            if (slicedXML.get(i).equals("<id>")) { // If the word is <id> tag
                int id = Integer.parseInt(slicedXML.get(i + 1)); // get the data after the <id> tag (id of the user)
                min = Math.min(id, min); // Setting the min
                max = Math.max(id, max); // Setting the max
            }
        }
    }

    //check if passed opening and closing tags match
    private boolean arePairedTags(String openingTag, String closingTag) {
        return removeAngleBrackets(openingTag).equals(closingTag.substring(2, closingTag.length() - 1));
    }

    //    O(1)
    private boolean isOpeningTag(String str) {
        return (isTag(str) // It is a tag^
                && str.charAt(1) != '/' // Not a closing tag
                && str.charAt(1) != '!' // Not an XML comment
                && str.charAt(1) != '?'); // Not a header
    }

    //    O(1)
    private boolean isClosingTag(String str) {
        return (isTag(str) && str.charAt(1) == '/');
    }

    //    O(1)
    // Checks if the input string is a tag (opening, closing, header or comment) or not
    boolean isTag(String str) {
        return (str.charAt(0) == '<' // Start with <
                && str.charAt(str.length() - 1) == '>'); // End with >
    }

    //    O(1)
    // Removes the angle brackets of the input tag
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
