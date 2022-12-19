package com.mycompany.datastructuresproject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Compression {

    static String encodeToBinaryString(String[] charCodes, String toCode) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < toCode.length(); i++) {
            code.append(charCodes[toCode.charAt(i)]);
        }
        //add padding to make the code multiple of 8 (can be converted to bytes)
        int padding = 8 - code.length() % 8;
        padding = padding == 8 ? 0 : padding;
        for (int i = 0; i < padding; i++) {
            code.append('1');
        }
        StringBuilder codewithpadding = new StringBuilder();
        codewithpadding.append(padding + code.toString());
        return codewithpadding.toString();
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

    static void writeToFile(byte[] compressedBytes) {
        // create a file object for the current location
        OutputStream os;
        try {
            os = new FileOutputStream("compressedFile.txt");
            //illustrating write(byte[] b) method
            os.write(compressedBytes);

            //illustrating flush() method
            os.flush();
        } catch (IOException ex) {
            Logger.getLogger(Compression.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static byte[] readFromFile(File file) {
        byte[] fileContent = null;
        try {
            //read the compressed file
            fileContent = Files.readAllBytes(file.toPath());
        } catch (IOException ex) {
            Logger.getLogger(Compression.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileContent;
    }

    static String bytesToBinaryString(byte[] bytes) {
        StringBuilder bytesToStore = new StringBuilder();
        //no of padding bits is not converted to binary
        bytesToStore.append((char) bytes[0]);
        for (int i = 1; i < bytes.length; i++) {
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

        return decompressed.toString();
    }

    static HuffmanTree compress(String s) {
        HuffmanTree tree = new HuffmanTree(s);
        String[] charCodes = tree.charCodes();
        String binaryString = encodeToBinaryString(charCodes, s);
        byte[] out = binaryStringToBytes(binaryString);
        writeToFile(out);
        return tree;
    }

    static String decompress(File compressedFile, HuffmanTree tree) {
        byte[] compressed = readFromFile(compressedFile);
        String binaryString = bytesToBinaryString(compressed);
        String decompressed = decodeBinaryString(tree, binaryString);
        return decompressed;
    }
}

class HuffmanTree {

    private HuffmanNode root;

    //make the huffman tree
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
            //set it's char to null as it doesnt represent a char)
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
}

class HuffmanNode {

    private char character;
    private int freq;
    private HuffmanNode left;
    private HuffmanNode right;

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
