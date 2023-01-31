package com.example.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Compression {
    static String paddingToBinaryString(String binaryString) {
        //add padding to make the code multiple of 8 (can be converted to bytes)
        //calculate no of padding bits needed
        int padding = 8 - binaryString.length() % 8;
        padding = padding == 8 ? 0 : padding;
        StringBuilder toAdd = new StringBuilder();
        for (int i = 0; i < padding; i++) {   //add padding bits
            toAdd.append('1');
        }
        return padding + binaryString + toAdd.toString();
    }

    //replace each char with its code
    static String encodeToBinaryString(String[] charCodes, String toCode) {
        StringBuilder code = new StringBuilder();
        //loop on each char --> replace its code from frequency array
        for (int i = 0; i < toCode.length(); i++) {
            code.append(charCodes[toCode.charAt(i)]);
        }
        //add padding then return the result
        return paddingToBinaryString(code.toString());
    }

    //takes the binary string and convert it to bytes
    //Ex: "00010010" is converted to byte of value 18
    static byte[] binaryStringToBytes(String binaryString) {
        //calculate the no of bytes --> +1 for padding bit
        int size = 1 + (binaryString.length() - 1) / 8;
        byte[] bytes = new byte[size];      //array to store the bytes
        bytes[0] = (byte) binaryString.charAt(0);   //store the padding bit
        int stored = 1;     //index for the bytes array
        char[] c = binaryString.toCharArray();

        //loop on every 8 char and calculate their value (binary to decimal)
        //then store it in a byte
        for (int i = 1; i < c.length; i += 8) {
            byte b = 0;
            int weight = 128;
            for (int j = 0; j < 8; j++) {
                b += (c[i + j] - '0') * weight;
                weight /= 2;
            }
            bytes[stored] = b;
            stored++;
        }
        return bytes;
    }

    //calculate the no of bytes of the encoded (traversed) tree, store it in 3 bytes as a 3 digit no.
    static byte[] TreeLengthToBytes(byte[] Tree) {
        int length = Tree.length;
        int hundreds = length / 100;                        //first digit
        int tens = (length - hundreds * 100) / 10;          //second digit
        int ones = (length - hundreds * 100 - tens * 10);   //third digit
        return new byte[]{(byte) hundreds, (byte) tens, (byte) ones};
    }

    //get the length of the tree from the first 3 bytes (digits)
    static int getTreeLength(byte[] Tree) {
        return Tree[0] * 100 + Tree[1] * 10 + Tree[2];
    }

    static void writeToFile(byte[] compressionTree, byte[] compressedBytes, String path) {
        // create a file object in the given path
        OutputStream os;
        byte[] treeLength = TreeLengthToBytes(compressionTree);
        try {
            os = new FileOutputStream(path);
            //illustrating write(byte[] b) method
            os.write(treeLength);           //first 3 bytes is the length of encoded compression Tree
            os.write(compressionTree);      //then store the compression tree
            os.write(compressedBytes);      //then we store the encoded xml file

            //illustrating flush() method
            os.flush();
        } catch (IOException ex) {
            // Ignored as we use file chooser in GUI so no possible errors will occur
            ex.getStackTrace();
        }
    }

    static byte[] readFromFile(File file) {
        byte[] fileContent = null;
        try {
            //read the compressed file
            fileContent = Files.readAllBytes(file.toPath());
        } catch (IOException ex) {
            // Ignored as we use file chooser in GUI so no possible errors will occur
            ex.getStackTrace();
        }
        return fileContent;
    }

    //extract the tree binary string from compressed bytes
    static String bytesToTreeBinaryString(byte[] bytes) {
        int length = getTreeLength(bytes);      // get length from first 3 bytes
        // form the binary string given the length of the tree
        return bytesToBinaryString(bytes, 3, length + 3);
    }

    static String bytesToFileBinaryString(byte[] bytes) {
        //skip the tree part by calculating the start after the end of the encoded tree bytes
        int start = getTreeLength(bytes) + 3;
        int end = bytes.length;
        //number of padding bit is not converted to binary
        return bytesToBinaryString(bytes, start, end);
    }

    static String bytesToBinaryString(byte[] bytes, int start, int end) {
        StringBuilder bytesToStore = new StringBuilder();
        //number of padding bit is not converted to binary
        bytesToStore.append((char) bytes[start]);
        //loop on each byte to convert it to binary string
        for (int i = start + 1; i < end; i++) {
            int weight = 128;
            int value;
            //make the range (0 --> 255) instead of (-128 --> 127)
            int currentByte = bytes[i] < 0 ? 256 + bytes[i] : bytes[i];
            //encode to 8 bits (each bit is stored as char in binary string)
            for (int j = 0; j < 8; j++) {
                value = currentByte >= weight ? 1 : 0;
                bytesToStore.append(value);
                if (value == 1) {
                    currentByte -= weight;
                }
                weight /= 2;
            }
        }
        return bytesToStore.toString();
    }

    static String decodeBinaryString(HuffmanTree tree, String BinaryString) {
        //get no of padding bits added at the end and convert it to int
        int padding = BinaryString.charAt(0) - '0';
        StringBuilder decompressed = new StringBuilder();
        int i = 1;
        HuffmanNode node = tree.getRoot();

        //loop on all bits neglecting the padding bits
        while (i < BinaryString.length() - padding) {
            //when reaching leaf node, it's char is added to the decompressed string
            if (node.isLeafNode()) {
                decompressed.append(node.getCharacter());
                node = tree.getRoot();
                continue;
            }
            //for each bit if 0 go left, if 1 go right until reaching a leaf node
            if (BinaryString.charAt(i) == '0') {
                node = node.getLeft();
            } else {
                node = node.getRight();
            }
            i++;
        }
        //add the last node reached at the end of the while (not added in the while)
        if (node.isLeafNode()) {
            decompressed.append(node.getCharacter());
        }
        return decompressed.toString();
    }

    static void compress(String s, String path) {
        HuffmanTree tree = new HuffmanTree(s); //form the huffman tree from the string to compress
        String traverse = tree.preOrderTraverse(); //form the binary string of the pre-order traverse of the tree
        traverse = paddingToBinaryString(traverse); //add padding to the binary string of the tree
        String[] charCodes = tree.charCodes();  //get the char code of each leaf node in the tree
                                                // and store it in a frequency array
        //encode the string to compress (replace each char by its code)
        String binaryString = encodeToBinaryString(charCodes, s);

        byte[] outTree = binaryStringToBytes(traverse); //convert the encoded(traversed) tree to bytes
        byte[] outCompression = binaryStringToBytes(binaryString); //convert the encoded string to bytes
        //write the tree and the compressed bytes to a file
        writeToFile(outTree, outCompression, path);
    }

    static String decompress(File compressedFile) {
        byte[] compressed = readFromFile(compressedFile);   //read all bytes in the file
        String TreebinaryString = bytesToTreeBinaryString(compressed);  //extract the binary string of the tree
        String FilebinaryString = bytesToFileBinaryString(compressed);  //extract the binary string of the compressed string

        //form the tree from its pre-order traverse (its binary string)
        HuffmanTree tree = new HuffmanTree(TreebinaryString.toCharArray());
        //form the decompressed file using the tree and the file binary string
        String decompressed = decodeBinaryString(tree, FilebinaryString);
        return decompressed;
    }

    //main for testing compression
    public static void main(String[] args) throws FileNotFoundException, IOException {
        /*XML xml = new XML(new File("sample.xml"));
        HuffmanTree tree = new HuffmanTree(xml.getXml());
        String s = tree.preOrderTraverse();
        System.out.println(s);
        HuffmanTree t = new HuffmanTree(s.toCharArray());
        s = t.preOrderTraverse();
        System.out.println(s);
        writeToFile(new byte[]{60,61,62,63},new byte[]{64,65,67,68},"fileName");
        byte[] compressed = readFromFile(new File("fileName"));
        String TreebinaryString = bytesToTreeBinaryString(compressed);
        String FilebinaryString = bytesToFileBinaryString(compressed);
        System.out.println(TreebinaryString);
        System.out.println(FilebinaryString);*/

    }
}

class HuffmanTree {

    private HuffmanNode root;

    //Default constructor
    public HuffmanTree() {}
    //make the huffman tree


    //constructor to construct the tree using its pre-order traverse (encoded tree)
    //used during decompressing
    public HuffmanTree(char[] c) {
        //form the tree by calling the recursive function get branch
        TraverseNode root = getBranch(1, c);
        this.root = root.node;
    }

    //construct the tree using the string to compress
    public HuffmanTree(String xml) {
        int[] Charfreq = new int[128];      //initialized to zeros
        char[] xmlChars = xml.toCharArray();
        for (char c : xmlChars) {              //loop on each char and increment its frequency
            Charfreq[(int) c] += 1;
        }
        //Make PriorityQueue and store each char in it making lowest frequency has highest priority.
        //This FrequencyComparator implemented at the bottom of the file
        //used to compare the huffman node according to their frequency.
        PriorityQueue<HuffmanNode> pQueue = new PriorityQueue<>(new FrequencyComparator());
        for (int i = 0; i < Charfreq.length; i++) {
            if (Charfreq[i] != 0) {
                pQueue.add(new HuffmanNode((char) i, Charfreq[i]));
            }
        }
        while (pQueue.size() != 1) {
            HuffmanNode left = pQueue.poll(); //left first to make it lower frequency
            HuffmanNode right = pQueue.poll();

            int frequency = left.getFreq() + right.getFreq();

            //make a parent node to hold these nodes
            //set its char to null as it doesn't represent a char
            //it's frequency is sum of freq of it's children then push to the pQueue
            pQueue.add(new HuffmanNode((char) 0, frequency, left, right));
        }
        //At the end of the while loop, only one node is still in the pQueue
        //This node is the root of the tree
        this.root = pQueue.poll();
    }

    //getter to the root of the tree
    public HuffmanNode getRoot() {
        return root;
    }

    //A recursive function that traverse the tree to get each char code
    // and fill the input string array
    void charCodes(String[] codes, HuffmanNode node, String str) {
        //base case for recursion
        if (node == null) {
            return;
        }
        //check if leaf node (represent a char in xml)
        //if it's the only node in the tree set its value to 1
        //else set the value to the input string
        if (node.getLeft() == null && node.getRight() == null) {
            codes[(int) node.getCharacter()] = str.length() > 0 ? str : "1";
        }
        //if it's a leaf node these to lines do nothing as both children are null, so we return
        charCodes(codes, node.getLeft(), str + "0");    //if going left, add 0 to the string
        charCodes(codes, node.getRight(), str + "1");   //if going right, add 1 to the string
    }

    //Fills a frequency array with each char code
    //to make it accessible in O(1), each char c access its code by index [(int)c]
    //the size of the array is 128 --> no of ascii char as XML files contain only ascii chars
    String[] charCodes() {
        String[] codes = new String[128];
        //call the recursive function to traverse the tree filling the char codes
        charCodes(codes, this.root, "");
        return codes;
    }

    //return a preorder traverse of the tree in which
    //each parent node is stored as 1
    //each leaf node is stored as the binary value of it's char
    //Ex: Node contain char 'a' (ascii = 97) is stored as 01100001
    String preOrderTraverse() {
        return preOrderTraverse(this.root); //calls a recursive function to traverse the whole tree
    }

    //pre-order traverse recursive function
    static String preOrderTraverse(HuffmanNode Node) {
        StringBuilder traverse = new StringBuilder();
        //if it's a leaf node --> it contains a char in range[0,127], so it has first bit zero
        //store the binary code of the char
        if (Node.isLeafNode()) {
            char c = Node.getCharacter();
            int weight = 128;
            int value;
            for (int j = 0; j < 8; j++) {
                value = c >= weight ? 1 : 0;
                traverse.append(value);
                if (value == 1) {
                    c -= weight;
                }
                weight /= 2;
            }
            //if it's a parent node store 1, as no leaf node starts with 1
            //then traverse the left and right subtrees
        } else {
            traverse.append('1');
            traverse.append(preOrderTraverse(Node.getLeft()));
            traverse.append(preOrderTraverse(Node.getRight()));
        }
        //return the traversed string
        return traverse.toString();
    }

    //A recursive function used to form the branch by forming the node and checking
    // if it's not a leaf node --> call the function to get branch for left and right subtrees
    TraverseNode getBranch(int i, char[] c) {
        //if we found a bit zero then it's [0,127] (ascii char), it's a char --> leaf node
        //it's value is calculated using the 8 bits binary to ascii code
        //then we form the node and return the index reached with the node in a traverse node
        if (c[i] == '0') {
            char b = 0;
            int weight = 128;
            for (int j = 0; j < 8; j++) {
                b += (c[i + j] - '0') * weight;
                weight /= 2;
            }
            HuffmanNode node = new HuffmanNode(b);
            return new TraverseNode(node, i + 8);
        }
        //else if we found a bit 1, it's a parent node

        // we create the node
        HuffmanNode node = new HuffmanNode((char) 0);
        //call get branch to get the left branch
        TraverseNode left = getBranch(i + 1, c);
        //store the root of the left branch as the left child
        node.setLeft(left.node);
        //call get branch to get the left branch, starting from the returned index
        // from the left branch call (last bit visited)
        TraverseNode right = getBranch(left.branchLastIndex, c);
        //store the root of the right branch as the right child
        node.setRight(right.node);
        //then we return the whole branch and the index for right subtree (last bit visited)
        // in a traverse node
        return new TraverseNode(node, right.branchLastIndex);
    }
}

//A class used to store the root of the branch formed by get branch function
// and the index of the reached bit in the encoded tree
class TraverseNode {
    HuffmanNode node;
    int branchLastIndex;

    public TraverseNode(HuffmanNode node, int branchLastIndex) {
        this.node = node;
        this.branchLastIndex = branchLastIndex;
    }
}

class HuffmanNode {

    private char character;
    private int freq;
    private HuffmanNode left;
    private HuffmanNode right;

    //constructor sets only the char
    // (mainly used when forming the tree from the encoded tree during decompression)
    public HuffmanNode(char character) {
        this.character = character;
    }

    //constructor that set only the char and the freq. (mainly used for leaf nodes)
    public HuffmanNode(char character, int freq) {
        this.character = character;
        this.freq = freq;
    }

    //constructor that set all the data fields (mainly used for the parent nodes)
    public HuffmanNode(char character, int freq, HuffmanNode left, HuffmanNode right) {
        this.character = character;
        this.freq = freq;
        this.left = left;
        this.right = right;
    }

    //getter for the node characteer
    public char getCharacter() {
        return character;
    }

    //getter for the frequency of a node
    public int getFreq() {
        return freq;
    }

    //getter for left child
    public HuffmanNode getLeft() {
        return left;
    }

    //getter for right child
    public HuffmanNode getRight() {
        return right;
    }

    //setter for left child
    public void setLeft(HuffmanNode left) {
        this.left = left;
    }

    //setter for right child
    public void setRight(HuffmanNode right) {
        this.right = right;
    }

    //check if the node is a leaf node
    boolean isLeafNode() {
        return left == null && right == null;
    }
}

//frequency comparator to compare the Huffman tree nodes according to their frequency
class FrequencyComparator implements Comparator<HuffmanNode> {

    @Override
    public int compare(HuffmanNode o1, HuffmanNode o2) {
        if (o1.getFreq() > o2.getFreq()) {
            return 1;
        }
        if (o1.getFreq() < o2.getFreq()) {
            return -1;
        }
        return 0;
    }
}
