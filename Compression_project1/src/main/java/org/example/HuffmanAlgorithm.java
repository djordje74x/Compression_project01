package org.example;

import org.example.models.FrequencyTable;
import org.example.models.Node;

import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import static org.example.util.EntropyCalculator.calculateFrequency;

public class HuffmanAlgorithm {

    public static void compress(String inputPath, String outputPath) throws IOException {
        File inputFile = new File(inputPath);
        if (inputFile.length() == 0){
            throw new IOException("Empty file");
        }
        FrequencyTable frequencyTable = calculateFrequency(inputFile);
        Node root = createTree(frequencyTable);

        Map<Integer, String> codeMap = new HashMap<>();
        generateCodes(root, "", codeMap);

        try (
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputPath));
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputPath));
        ) {
            dos.writeLong(inputFile.length());

            dos.writeInt(codeMap.size());

            int symbol;
            String code;
            for (var entry : codeMap.entrySet()) {
                symbol = entry.getKey();
                code = entry.getValue();

                dos.writeByte(symbol);
                dos.writeByte(code.length());

                int tempByte = 0;
                int bitCounter = 0;
                for(char c: code.toCharArray()) {
                    tempByte = (tempByte << 1) | (c == '1' ? 1 : 0);
                    bitCounter++;
                    if(bitCounter == 8) {
                        dos.writeByte(tempByte);
                        bitCounter = 0;
                        tempByte = 0;
                    }
                }
                if(bitCounter > 0){
                    tempByte <<= (8 - bitCounter);
                    dos.writeByte(tempByte);
                }
            }

            int buffer = 0;
            int bitCounter = 0;
            int byteRead;

            while ((byteRead = bis.read()) != -1) {
                code = codeMap.get(byteRead & 0xFF);
                for (char bit : code.toCharArray()) {
                    buffer = (buffer << 1) | (bit == '1' ? 1 : 0);
                    bitCounter++;
                    if (bitCounter == 8) {
                        dos.writeByte(buffer);
                        bitCounter = 0;
                        buffer = 0;
                    }
                }
            }
            if (bitCounter > 0) {
                buffer <<= (8 - bitCounter);
                dos.writeByte(buffer);
            }
        }
    }

    public static void decompress(String inputPath, String outputPath) throws IOException {
        try (
                DataInputStream dis = new DataInputStream(new FileInputStream(inputPath));
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputPath))
        ) {
            long length = dis.readLong();
            int tableSize = dis.readInt();
            Map<Integer, String> reverseCodeMap = new HashMap<>(tableSize);

            int byteValue, codeLength, read, one, bit;

            for (int i = 0; i < tableSize; i++) {
                byteValue = dis.readUnsignedByte();
                codeLength = dis.readByte() & 0xFF;
                StringBuilder builder = new StringBuilder();
                read = 0;

                while (read < codeLength) {
                    one = dis.readUnsignedByte();
                    for (int j = 7; j >= 0 && read < codeLength; j--) {
                        bit = (one >> j) & 1;
                        builder.append(bit == 1 ? '1' : '0');
                        read++;
                    }
                }
                reverseCodeMap.put(byteValue, builder.toString());
            }

            Node root = recreateTree(reverseCodeMap);
            Node currentNode = root;
            long written = 0;
            int dataByte;

            while (written < length) {
                dataByte = dis.readUnsignedByte();
                for (int j = 7; j >= 0 && written < length; j--) {
                    bit = (dataByte >> j) & 1;
                    currentNode = (bit == 0) ? currentNode.getLeft() : currentNode.getRight();
                    if (currentNode.isLeaf()) {
                        bos.write(currentNode.getValue());
                        currentNode = root;
                        written++;
                    }
                }
            }
        }
    }

    private static void generateCodes(Node node, String code, Map<Integer, String> codeMap) {
        if (node.isLeaf()) {
            codeMap.put(node.getValue(), code.isEmpty() ? "0" : code);
            return;
        }
        generateCodes(node.getLeft(), code + "0", codeMap);
        generateCodes(node.getRight(), code + "1", codeMap);
    }

    private static Node createTree(FrequencyTable frequencyTable) {
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingLong(Node::getFrequency));

        long freq;

        for (int i = 0; i < 256; i++) {
            freq = frequencyTable.getCountForSymbol(i);
            if (freq > 0) {
                queue.add(new Node(i, freq));
            }
        }

        if (queue.isEmpty()){
            return null;
        }

        while (queue.size() > 1) {
            Node leftNode = queue.poll();
            Node rightNode = queue.poll();
            Node rootNode = new Node(leftNode, rightNode);
            queue.add(rootNode);
        }
        return queue.poll();
    }

    private static Node recreateTree(Map<Integer, String> codeMap) {
        Node rootNode = new Node(-1, 0);
        int symbol;
        String code;
        for (var entry : codeMap.entrySet()) {
            symbol = entry.getKey();
            code = entry.getValue();
            insertNode(rootNode, symbol, code);
        }
        return rootNode;
    }

    private static void insertNode(Node node, int symbol, String code) {
        Node currentNode = node;
        for (char bit : code.toCharArray()) {
            if (bit == '0') {
                if (currentNode.getLeft() == null) currentNode.setLeft(new Node(-1, 0));
                currentNode = currentNode.getLeft();
            } else {
                if (currentNode.getRight() == null) currentNode.setRight(new Node(-1, 0));
                currentNode = currentNode.getRight();
            }
        }
        currentNode.setValue(symbol);
    }
}
