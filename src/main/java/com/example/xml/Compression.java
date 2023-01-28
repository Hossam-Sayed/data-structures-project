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
    static String paddingToBinaryString(String binaryString){
        //add padding to make the code multiple of 8 (can be converted to bytes)
        int padding = 8 - binaryString.length() % 8;
        padding = padding == 8 ? 0 : padding;
        StringBuilder toAdd = new StringBuilder();
        for (int i = 0; i < padding; i++) {
            toAdd.append('1');
        }
        return padding + binaryString + toAdd.toString();
    }
    static String encodeToBinaryString(String[] charCodes, String toCode) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < toCode.length(); i++) {
            code.append(charCodes[toCode.charAt(i)]);
        }
        return paddingToBinaryString(code.toString());
    }

    static byte[] binaryStringToBytes(String binaryString) {
        int size = 1 + (binaryString.length() - 1) / 8;
        byte[] bytes = new byte[size];
        bytes[0] = (byte) binaryString.charAt(0);
        int stored = 1;
        char[] c = binaryString.toCharArray();
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

    static byte[] TreeLengthToBytes(byte[] Tree){
        int length = Tree.length;
        int hundreds = length/100;
        int tens = (length - hundreds*100)/10;
        int ones = (length - hundreds*100 - tens * 10);
        return new byte[]{(byte) hundreds, (byte) tens, (byte) ones};
    }

    static int getTreeLength(byte[] Tree){
        return Tree[0]*100 + Tree[1]*10 + Tree[2];
    }

    static void writeToFile(byte[] compressionTree,byte[] compressedBytes, String fileName) {
        // create a file object for the current location
        OutputStream os;
        byte[] treeLength = TreeLengthToBytes(compressionTree);
        try {
            os = new FileOutputStream(fileName);
            //illustrating write(byte[] b) method
            os.write(treeLength);
            os.write(compressionTree);
            os.write(compressedBytes);

            //illustrating flush() method
            os.flush();
        } catch (IOException ex) {
//            Logger.getLogger(Compression.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static byte[] readFromFile(File file) {
        byte[] fileContent = null;
        try {
            //read the compressed file
            fileContent = Files.readAllBytes(file.toPath());
        } catch (IOException ex) {
//            Logger.getLogger(Compression.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileContent;
    }

    static String bytesToTreeBinaryString(byte[] bytes){
        int length = getTreeLength(bytes);
        return bytesToBinaryString(bytes,3,length+3);
    }

    static String bytesToFileBinaryString(byte[] bytes) {
        //skip the tree part
        int start = getTreeLength(bytes) + 3;
        int end = bytes.length ;
        //no of padding bits is not converted to binary
        return bytesToBinaryString(bytes,start,end);
    }

    static String bytesToBinaryString(byte[] bytes,int start, int end) {
        StringBuilder bytesToStore = new StringBuilder();
        //no of padding bits is not converted to binary
        bytesToStore.append((char) bytes[start]);
        for (int i = start + 1; i < end; i++) {
            int weight = 128;
            int value;
            int currentByte = bytes[i] < 0 ? 256 + bytes[i] : bytes[i];
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
        while (i < BinaryString.length() - padding) {
            if (node.isLeafNode()) {
                decompressed.append(node.getCharacter());
                node = tree.getRoot();
                continue;
            }
            if (BinaryString.charAt(i) == '0') {
                node = node.getLeft();
            } else {
                node = node.getRight();
            }
            i++;
        }
        if (node.isLeafNode()) {
            decompressed.append(node.getCharacter());
        }
        return decompressed.toString();
    }

    static void compress(String s,String fileName) {
        HuffmanTree tree = new HuffmanTree(s);
        String traverse = tree.preOrderTraverse();
        traverse = paddingToBinaryString(traverse);
        String[] charCodes = tree.charCodes();
        String binaryString = encodeToBinaryString(charCodes, s);
        byte[] outTree = binaryStringToBytes(traverse);
        byte[] outCompression = binaryStringToBytes(binaryString);
        writeToFile(outTree,outCompression,fileName);
    }

    static String decompress(File compressedFile) {
        byte[] compressed = readFromFile(compressedFile);
        String TreebinaryString = bytesToTreeBinaryString(compressed);
        String FilebinaryString = bytesToFileBinaryString(compressed);
        HuffmanTree tree = new HuffmanTree(TreebinaryString.toCharArray());
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
    private int traverseIndex;
    public HuffmanTree(){}
    //make the huffman tree

    //construct the tree using its pre-order traverse
    public HuffmanTree (char[] c) {
        TraverseNode root = getBranch(1, c);
        this.root = root.node;
    }

    //construct the tree using the string to compress
    public HuffmanTree(String xml) {
        int[] Charfreq = new int[128];      //initialized to zeros
        char[] xmlChars = xml.toCharArray();
        for (char c : xmlChars) {              //loop on each char and increment it's frequency
            Charfreq[(int) c] += 1;
        }
        //make PriorityQueue and store each char in it making lowest frequency has highest priority
        /*this FrequencyComparator implemented downward used to 
        compare the huffman node according to their frequency*/
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
            //it's frequency is sum of freq of it's children
            pQueue.add(new HuffmanNode((char) 0, frequency, left, right));
        }
        this.root = pQueue.poll();
    }

    public HuffmanNode getRoot() {
        return root;
    }

    void charCodes(String[] codes, HuffmanNode node, String str) {
        if (node == null) {
            return;
        }
        //check if leaf node (represent a char in xml)
        if (node.getLeft() == null && node.getRight() == null) {
            codes[(int) node.getCharacter()] = str.length() > 0 ? str : "1";
        }
        charCodes(codes, node.getLeft(), str + "0");
        charCodes(codes, node.getRight(), str + "1");
    }

    //fill a frequency array with each char code
    //to make it accessible in O(1), each char c access it's code by index [(int)c]
    String[] charCodes() {
        String[] codes = new String[128];               //it's size is no of ascii char
        charCodes(codes, this.root, "");
        return codes;
    }
    String preOrderTraverse(){
        return preOrderTraverse(this.root);
    }

    //pre-oreder traverse
    static String preOrderTraverse(HuffmanNode Node) {
        StringBuilder traverse = new StringBuilder();
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
        } else {
            traverse.append('1');
            traverse.append(preOrderTraverse(Node.getLeft()));
            traverse.append(preOrderTraverse(Node.getRight()));
        }
        return traverse.toString();
    }

    TraverseNode getBranch(int i, char[] c){
        if(c[i] == '0'){
            char b = 0;
            int weight = 128;
            for (int j = 0; j < 8; j++) {
                b += (c[i + j] - '0') * weight;
                weight /= 2;
            }
            HuffmanNode node = new HuffmanNode(b);
            return new TraverseNode(node, i+8);
        }
        HuffmanNode node = new HuffmanNode((char) 0);
        TraverseNode left = getBranch(i+1, c);
        node.setLeft(left.node);
        TraverseNode right = getBranch(left.branchLastIndex, c);
        node.setRight(right.node);
        return new TraverseNode(node,right.branchLastIndex);
    }
}

class TraverseNode{
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

    public HuffmanNode(char character) {
        this.character = character;
    }
    
    public HuffmanNode(char character, int freq) {
        this.character = character;
        this.freq = freq;
    }

    public HuffmanNode(char character, int freq, HuffmanNode left, HuffmanNode right) {
        this.character = character;
        this.freq = freq;
        this.left = left;
        this.right = right;
    }

    public char getCharacter() {
        return character;
    }

    public int getFreq() {
        return freq;
    }

    public HuffmanNode getLeft() {
        return left;
    }

    public HuffmanNode getRight() {
        return right;
    }

    public void setLeft(HuffmanNode left) {
        this.left = left;
    }

    public void setRight(HuffmanNode right) {
        this.right = right;
    }

    boolean isLeafNode() {
        return left == null && right == null;
    }
}

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
