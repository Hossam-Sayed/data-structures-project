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

    private boolean valid = false;  //if true --> file is checked and valid, so we save the check time
    //if false --> file is unchecked or not valid, so we check again
    private boolean sliced = false; //if true --> file is sliced, else call slice if slicedXML is needed
    private String xml; // A string contain the whole XML file
    private Tree xmlTree; // Tree representation of the XML file
    private Graph xmlGraph; // Graph representation of the XML file
    private ArrayList<String> slicedXML;    //Store the XML file as slices
    //Each slice is either a tag or an attribute value


    //A constructor that construct the xml object from an input file
    //O(n), where n is the number of char in xml file
    XML(File file) {
        BufferedReader reader;
        StringBuilder xml = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            //read line by line and append it to the string builder
            while (line != null) {
                xml.append(line);
                xml.append("\n");
                line = reader.readLine();
            }
            //delete the last appended '\n' added after the last line
            xml.deleteCharAt(xml.length() - 1);
            //convert the string builder to a string and store it in the xml object
            this.xml = xml.toString();
            reader.close();
        } catch (IOException e) {
            // Ignored as we use file chooser in GUI so no possible errors will occur
            e.getStackTrace();
        }
    }

    //A constructor that construct the xml object from an input String
    XML(String s) {
        this.xml = s;
    }

    //A getter to return the String xml
    public String getXml() {
        return xml;
    }

    //It calls the static method compress in compression class to compress the xml file
    //It's input is the path in which we store the compressed file
    void compress(String path) {
        Compression.compress(this.xml, path);
    }

    //It calls the static method decompress in compression class to decompress the xml file
    //It's output is the path of the compressed file which we want to decompress
    static String decompress(String path) {
        return Compression.decompress(new File(path));
    }

    //Its complexity is the same as getErrors function as it calls just getErrors
    //It sets the boolean fix to true to fix the errors while detecting it
    void fixErrors() {
        getErrors(true);
    }

    //Its functionality is done by the GUI, is it's not used
    boolean isValidOld() {
        //if valid is set to true, then the file is already checked and valid
        if (valid) {
            return true;
        }
        //if valid is false, the file is either not valid or not checked before
        //So we call getErrors to check the file
        //It sets the boolean fix to false, so it doesn't fix the errors
        ArrayList<String> errors = getErrors(false);
        //if no errors returned, then the file is valid, else it's not valid
        valid = errors == null;
        return errors == null;
    }

    //Getter to valid boolean
    public boolean isValid() {
        return valid;
    }

    //Loop on the whole file chars to check the consistency (validity) of the file.
    //O(n) --> n is no. of chars.
    //Boolean fix determines if we will fix the file while checking
    //or just validate and show the errors.
    ArrayList<String> getErrors(boolean fix) {

        ArrayList<String> errors = new ArrayList<>();   //used to store the errors
        Stack<String> openedTags = new Stack<>();       //push opened tag to check consistency
        StringBuilder fixedXML =new StringBuilder();    //used in case of fix to store the fixed XML
        boolean inTag = false;
        boolean hasValue = false;
        int line = 1;                               //track the current line to locate the errors
        String tag = "";
        char[] xmlChars = xml.toCharArray();

        //loop on every char in the file and locate the errors
        //if fix is true
        //      parts with no errors are appended to the fixedXML in case of
        //      parts with errors are fixed and appended to the fixedXML
        for (int i = 0; i < xmlChars.length; i++) {
            if (!inTag) {
                switch (xmlChars[i]) {
                    case '\n' -> {      //if '\n' is found, increment lines to keep track of current line
                        line++;
                        if (fix) {
                            fixedXML.append(xmlChars[i]);
                        }
                    }
                    case '<' -> {       //if not in tag and '<' is detected --> set inTag to True
                        inTag = true;
                        tag = "<";
                    }
                    default -> {        //any other Char with no tag opened is an error
                        if (!(xmlChars[i] == ' ' || xmlChars[i] == '\t')) { //not white spaces
                            if (openedTags.empty()) {
                                errors.add("Line " + line + ": Attribute value with no opened tags");
                                //skip the rest of the error char to avoid duplicate errors
                                while (i < xmlChars.length && xmlChars[i] != '\n' && xmlChars[i] != '<') {
                                    i++;
                                }
                            } else {                //if a tag is opened and we find data
                                hasValue = true;    //set hasValue to true to indicate that the current tag has an attribute value
                                //then we add all the attribute value char to the file
                                //the end of the attribute value is either a new line or a start of a tag
                                while (i < xmlChars.length && !(xmlChars[i] == '\n' || xmlChars[i] == '<')) {
                                    if (fix) {
                                        fixedXML.append(xmlChars[i]);
                                    }
                                    i++;
                                }
                            }
                            i--;
                        } else {        //else (white spaces) --> append to fixed XML
                            if (fix) {
                                fixedXML.append(xmlChars[i]);
                            }
                        }
                    }
                }
            } else {                            //if in tag (between '<' and '>')
                tag += xmlChars[i];             // add the char to a string tag until the end of tag '>'.
                if (xmlChars[i] == '>') {       //When reaching the end to the tag
                    if (isOpeningTag(tag)) {
                        if (!openedTags.empty() && hasValue) {  //if a tag is opened after an attribute
                            // value of another tag --> error --> a tag cannot contain a value and another child tag
                            errors.add("Line " + line + ": Tag " + openedTags.peek()
                                    + " must be closed before opening " + tag
                                    + " tag, as " + openedTags.peek() + " has attribute value");
                            if (fix) {                      //fix by closing the tag that has an attribute
                                fixedXML.append("</");      //value before opening the other tag
                                fixedXML.append(openedTags.peek().substring(1));
                            }
                            //pop that opened tag (old) from the stack, as it is either closed
                            // during fix or must before opening the other tag
                            openedTags.pop();
                            hasValue = false;
                        }
                        //push the opened tag to the stack and append it to the fixed XML
                        openedTags.push(tag);
                        if (fix) {
                            fixedXML.append(tag);
                        }
                    } else if (isClosingTag(tag)) {     //if the tag is closing tag
                        //if it's the closing tag of the last opened (top of the stack)
                        if (!openedTags.empty() && arePairedTags(openedTags.peek(), tag)) {
                            openedTags.pop();
                            hasValue = false;
                            if (fix) {
                                fixedXML.append(tag);
                            }
                            //if it's not the same as the top of the stack we check
                        } else if (!openedTags.empty() && !arePairedTags(openedTags.peek(), tag)) {
                            Stack<String> unclosedtags = new Stack<>();
                            while (!openedTags.empty() && !arePairedTags(openedTags.peek(), tag)) {
                                unclosedtags.push(openedTags.pop());
                            }
                            //if it's not in the stack --> error: a closing tag for no opening tag
                            //neglected (not appended to fixed XML)
                            if (openedTags.empty()) {
                                errors.add("Line " + line + ": No opening tag for closing tag " + tag);
                                while (!unclosedtags.empty()) {
                                    openedTags.push(unclosedtags.pop());
                                }
                            } else {
                                //if it's in the stack but includes some opening tags that is not closed
                                //error --> we must close these tags first
                                //fixed by appending closing tags for these tags to the fixed XML
                                //and pop their opening from the stack
                                while (!unclosedtags.empty()) {
                                    errors.add("Line " + line + ": Expected closing tag for " + unclosedtags.peek()
                                            + " before closing tag " + tag);
                                    if (fix) {
                                        fixedXML.append("</");
                                        fixedXML.append(unclosedtags.peek().substring(1));
                                    }
                                    unclosedtags.pop();
                                }
                                //Now it's matching the opening tag at the top of the stack
                                //then pop from stack and append to the fixed XML
                                openedTags.pop();
                                if (fix) {
                                    fixedXML.append(tag);
                                }
                            }
                        }
                    }
                    inTag = false;      //after handling the tag --> set inTag to false
                    tag = "";           //clear the String tag
                }
            }
        }
        //After reaching the end of the file, we check the opened tags stack
        //If it's not empty, then there is an error: missing closing tags for some opened tags
        //fixed by appending the closing tags of these tags
        while (!openedTags.empty()) {
            errors.add("Expected closing tag for " + openedTags.peek() + " before the end of the file");
            if (fix) {
                fixedXML.append("</");
                fixedXML.append(openedTags.peek().substring(1));
            }
            openedTags.pop();
        }
        //in case of fix, we replace the fixed XML with the current xml
        if (fix) {
            this.xml = fixedXML.toString();
        }
        //if there are errors, return the errors. Else return null indicating no errors
        return errors.isEmpty() ? null : errors;
    }

    //O(n), where n is the number of char in xml file
    void sliceXML() {
        boolean inValue = false;
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
                    inValue = true;
                }
            } else if (!inValue && (xmlchars[i] == ' ' || xmlchars[i] == '\t'));//Skip white spaces outside values
            else if (inValue && xmlchars[i + 1] == '<') { //slice before the closing tag
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
    //O(n), where n is the number of char if file is not sliced
    String minifyXML() {
        if (!sliced) {
            sliceXML();
        }
        StringBuilder minified = new StringBuilder();
        //concatenate all slices with no white spaces.
        //White spaces inside attribute values (like post body) is not removed (stored in the slices).
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


    // the recursive function tha do the format of the current node
    // its parameter is the indentation of the parent node and the other parameter is the node itself
    String formatingNode(String str, TreeNode node) {
        //creating a string builder that takes the indentation of the parent node and add the remaining part to be the indentation fot thr current node
        StringBuilder addition = new StringBuilder();
        addition.append(str).append("\t");
        //creating a string builder that saves the format of the current nodes and its children
        StringBuilder strs = new StringBuilder();
        strs.append("<").append(node.getTagName()).append(">"); // formatting the open tag of the current node
        ArrayList<TreeNode> childrens = node.getChildren(); //creating array list contains the children of the current node
        if (!childrens.isEmpty()) { //if the node is not a leaf node
            for (int i = 0; i < childrens.size(); i++) {
                // looping along the children and call the function again(recursively) with each child node
                strs.append(addition); //indentation for formatting the xml file
                strs.append(formatingNode(addition.toString(), childrens.get(i)));
            }
        } else { //if the node is a leaf node
            strs.append(addition).append(node.getData()).append(str).append("</").append(node.getTagName()).append(">"); //formatting of the open tag of leaf node
            return strs.toString(); // return the string
        }
        strs.append(str).append("</").append(node.getTagName()).append(">");// formation of the closed tag of the current node
        return strs.toString(); // return the string
    }
    //make the format of a xml file using the xml tree
    void format() {
        if (xmlTree == null) { //if there is no xmlTree for the xml file created
            this.xmlToTree(); //create the tree
        }
        String str = ""; //create a string that saves all the format of the xml in it
        TreeNode node = xmlTree.getRoot(); //get the node of the tree of the xml file
        if (node != null) {
            str = formatingNode("\n", node); //we call a recursive function that do the format of the current node
        }
        xml = str;
    }


    void stringToXmlFile(String path) {
        // create a file object for the choosed path
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
            // Ignored as we use file chooser in GUI so no possible errors will occur
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
            // Ignored as we use file chooser in GUI so no possible errors will occur
        }
    }

    // the recursive function tha do the format of the children nodes of the current node
    // its parameter is the indentation of the current node and the other parameter is the node itself
    String jsonFormattingNode(String str, TreeNode node) {
        //creating a string builder that takes the indentation of the current node
        StringBuilder addition = new StringBuilder();
        addition.append(str);
        //this addition will be always done except if the current node is a leaf node so we don't add any additions we just print its data at the end of the function
        if (!addition.toString().equals(" ")) {
            addition.append("\t"); //add the remaining part to be the indentation fot thr children nodes
        }
        StringBuilder strs = new StringBuilder();  //creating a string builder that saves the JSON format of the current nodes and its children
        ArrayList<TreeNode> childrens = node.getChildren(); //creating array list contains the children of the current node
        //we will sort the children nodes for some JSON formatting-purposes(to get all the child nodes that have the same tag name after each others)
        ArrayList<TreeNode> sortedChildrens = new ArrayList<>(); //creating empty array list
        int[] visitedChildrens = new int[childrens.size()]; // array to know if it has been sorted or not

        for (int i = 0; i < childrens.size(); i++) {
            if (visitedChildrens[i] == 1) //if the node is sorted already and have been added to the sorted array
                continue;

            sortedChildrens.add(childrens.get(i)); //add the node to the sorted array
            visitedChildrens[i] = 1; // mark that the node is sorted

            //check all the other nodes
            for (int j = 0; j < childrens.size(); j++) {
                if (childrens.get(i).getTagName().equals(childrens.get(j).getTagName()) && (i != j)) {
                    //if a tag name of a node is equal to the current node tag name
                    sortedChildrens.add(childrens.get(j)); // we will add the similar node to the array after the original
                    visitedChildrens[j] = 1; // and mark that it is sorted
                }
            }
        }

        if (!sortedChildrens.isEmpty()) { //check if the array is not empty
            for (int i = 0; i < sortedChildrens.size(); i++) { //go through all nodes
                if (i < sortedChildrens.size() - 1 && sortedChildrens.get(i).getTagName().equals(sortedChildrens.get(i + 1).getTagName())) {
                    //if the current node tag name equals to the consecutive node tag name after it in the sorted array (related to the JSON format)
                    strs.append(addition).append("\"").append(sortedChildrens.get(i).getTagName()).append("\":").append("[");
                    //we open an object array in the JSON format with this tag name(for JSON format purposes)
                    String s = sortedChildrens.get(i).getTagName();//we save this tag name in a string
                    while (i < sortedChildrens.size() && sortedChildrens.get(i).getTagName().equals(s)) {// for all nodes that have the same tag name
                        if (sortedChildrens.get(i).getChildren() != null && sortedChildrens.get(i).getChildren().size() >= 1) { //if the node have children
                            strs.append(addition).append("\t{"); //add the indentation with the start of the coming object (related to the JSON format)
                            strs.append(jsonFormattingNode(addition.toString() + "\t", sortedChildrens.get(i))); //recall the function again with this node
                            if (strs.charAt(strs.length() - 1) == ',') {//a condition to remove the last comma in the format (something for the JSON format)
                                strs.deleteCharAt(strs.length() - 1);
                            }
                            strs.append(addition).append("\t},");// add the end of the object (related to the JSON format)
                        } else {//if the node doesn't have children
                            strs.append(jsonFormattingNode(addition.toString(), sortedChildrens.get(i)));
                        }
                        i++;
                    }
                    i--;
                    if (strs.charAt(strs.length() - 1) == ',') {//a condition to remove the last comma in the format (something for the JSON format)
                        strs.deleteCharAt(strs.length() - 1);
                    }
                    strs.append(addition).append("],"); // end of the array format in JSON format
                } else {//if the current node tag name is not equal to the consecutive node tag name after it in the sorted array
                    if (sortedChildrens.get(i).getChildren() != null
                            && !sortedChildrens.get(i).getChildren().isEmpty()) { //we check if this node have children
                        strs.append(addition).append("\"").append(sortedChildrens.get(i).getTagName()).append("\":").append("{"); //format the node
                        strs.append(jsonFormattingNode(addition.toString(), sortedChildrens.get(i)));//recall the function with this node
                        if (strs.charAt(strs.length() - 1) == ',') {//a condition to remove the last comma in the format (something for the JSON format)
                            strs.deleteCharAt(strs.length() - 1);
                        }
                        strs.append(addition).append("},");//add the end for the formation
                    } else {//if this node doesn't have children
                        strs.append(addition).append("\"").append(sortedChildrens.get(i).getTagName()).append("\":");//format the node
                        strs.append(jsonFormattingNode(" ", sortedChildrens.get(i)));//we recall the function to print the data for the node
                    }
                }


            }

        } else { //if the sorted array is empty then there is no children for the current nodes,then the current node is a leaf node
            strs.append(addition).append("\"").append(node.getData()).append("\"").append(","); //we will print its data
        }
        return strs.toString(); //return the JSON format

    }
    //make the JSON format of a xml file using the xml tree
    // this function is the same as the xml format but the difference is that the recursive function makes the format of the child nodes not the current node
    String xmlToJson() {
        if (xmlTree == null) {  //if there is no xmlTree for the xml file created
            this.xmlToTree(); //create the tree
            //  xmlToJson();
        }
        StringBuilder str = new StringBuilder(); //create a string builder that saves all the JSON format of the xml in it
        str.append("{\n"); //the starting of the JSON format
        TreeNode node = xmlTree.getRoot(); //get the node of the tree of the xml file
        if (node != null) {
            str.append("\t").append("\"").append(node.getTagName()).append("\"").append(":{"); // format the tag name of the root node
            str.append(jsonFormattingNode("\n\t", node)); //we call a recursive function that do the JSON format of the child nodes of the current node
        }
        // format the end of the root node
        if (str.charAt(str.length() - 1) == ',') { //a condition to remove the last comma in the format (something for the JSON format)
            str.deleteCharAt(str.length() - 1);
        }
        str.append("\n\t}").append("\n}"); //the end of the JSON format
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
