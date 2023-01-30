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
        if (!valid) {
            return;
        } else if (!sliced) {
            sliceXML();
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
                        if (s.peek().equals("<follower>")) {  // Current ID belongs to a follower
                            if (dummy[index] == null) { // If we haven't met this follower before
                                dummy[index] = new User(); // Add this follower to dummy list
                            }
                            dummy[index].incFollows(); // Increment the follower's follows by one
                            user.addFollower(dummy[index]); // Add this follower to followers list of current user
                        } else { // Current ID belongs to a user
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


    String jsonFormatingNode(String str, TreeNode node) {
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
                            strs.append(jsonFormatingNode(addition.toString() + "\t", sortedChildrens.get(i)));
                            if (strs.charAt(strs.length() - 1) == ',') {
                                strs.deleteCharAt(strs.length() - 1);
                            }
                            strs.append(addition + "\t},");
                        } else {
                            strs.append(jsonFormatingNode(addition.toString(), sortedChildrens.get(i)));
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
                        strs.append(jsonFormatingNode(addition.toString(), sortedChildrens.get(i)));
                        if (strs.charAt(strs.length() - 1) == ',') {
                            strs.deleteCharAt(strs.length() - 1);
                        }
                        strs.append(addition + "},");
                    } else {
                        strs.append(addition + "\"" + sortedChildrens.get(i).getTagName() + "\":");
                        strs.append(jsonFormatingNode(" ", sortedChildrens.get(i)));
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
            str.append(jsonFormatingNode("\n\t", node));
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
        try {
            ArrayList<User> mutualFollowers = new ArrayList<>();
            User dummy[] = new User[max - min + 1];
            ArrayList<User> users = xmlGraph.getUsers();

            int i;
            for (i = 0; i < users.size(); i++) {
                if (users.get(i).getId().equals(id1)) {
                    for (User follower : users.get(i).getFollowers()) {
                        int index = Integer.parseInt(follower.getId()) - min;
                        dummy[index] = follower;
                    }
                    break;
                }
            }

            if (i == users.size()) {
                return null;
            }

            for (i = 0; i < users.size(); i++) {
                if (users.get(i).getId().equals(id2)) {
                    for (User follower : users.get(i).getFollowers()) {
                        int index = Integer.parseInt(follower.getId()) - min;
                        if (dummy[index] != null) {
                            mutualFollowers.add(dummy[index]);
                        }
                    }
                    break;
                }
            }

            if (i == users.size()) {
                return null;
            }

            return mutualFollowers;
        } catch (Exception e) {
            return null;
        }
    }

    enum Relativity {FOLLOWER, ME}

    public ArrayList<User> suggestFollowers(String id) {
        try {
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
            if (Integer.parseInt(id) > max || Integer.parseInt(id) < min) return null;
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
            return suggestedList;
        } catch (Exception e) {
            return null;
        }
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

        return searchedPosts;
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

    //check if passed opening and closing tags match
    private boolean arePairedTags(String openingTag, String closingTag) {
        return removeAngleBrackets(openingTag).equals(closingTag.substring(2, closingTag.length() - 1));
    }

    private boolean isOpeningTag(String str) {
        return (isTag(str) // It is a tag^
                && str.charAt(1) != '/' // Not a closed tag
                && str.charAt(1) != '!' // Not a comment
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
