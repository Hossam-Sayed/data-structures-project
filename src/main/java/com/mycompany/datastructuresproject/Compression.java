package com.mycompany.datastructuresproject;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Compression {

    static String coddedString(String[] charCodes, String toCode) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < toCode.length(); i++) {
            code.append(charCodes[toCode.charAt(i)]);
        }
        //add padding to make the code multiple of 8 (can be converted to bytes)
        int padding = 8 - code.length() % 8;
        for (int i = 0; i < padding; i++) {
            code.append('1');
        }
        StringBuilder codewithpadding = new StringBuilder();
        codewithpadding.append(padding + code.toString());
        return codewithpadding.toString();
    }

    static String coddedStringToBytes(String coddedString) {
        StringBuilder bytesToStore = new StringBuilder();
        bytesToStore.append(coddedString.charAt(0));
        char[] c = coddedString.toCharArray();
        for (int i = 1; i < c.length; i += 8) {
            char b = 0;
            int weight = 128;
            for (int j = 0; j < 8; j++) {
                b += (c[i + j] - '0') * weight;
                weight /= 2;
            }
            bytesToStore.append(b);
        }
        return bytesToStore.toString();
    }

    static void writeToFile(String json) {
        // create a file object for the current location
        File file = new File("compressedFile.txt");
        try {
            // create a new file with name specified
            // by the file object
            boolean value = file.createNewFile();
            if (value) {
                //    System.out.println("New Java File is created.");
            } else {
                //    System.out.println("The file already exists.");
            }
            try (FileWriter output = new FileWriter("compressedFile.txt")) {
                output.write(json);
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    static void compress(String s) {
        HuffmanTree test = new HuffmanTree(s);
        String[] charCodes = test.charCodes();
        String h = coddedString(charCodes, s);
        String out = coddedStringToBytes(h);
        writeToFile(out);
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
