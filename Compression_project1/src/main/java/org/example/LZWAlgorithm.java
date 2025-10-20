package org.example;

import java.io.*;
import java.util.*;

import static org.example.util.ByteUtil.readFileToByteArray;

public class LZWAlgorithm {
    private static final int MAX_DICT_SIZE = 4096;

    public static void compress(String inputPath, String outputPath) throws IOException {
        byte[] input = readFileToByteArray(inputPath);

        Map<String, Integer> dict = new HashMap<>(512);
        for (int i = 0; i < 256; i++) {
            dict.put("" + (char) i, i);
        }
        int dictSize = 256;

        List<Integer> codeList = new ArrayList<>(input.length);
        String w = "";
        for (byte b : input) {
            char c = (char) (b & 0xFF);
            String wc = w + c;
            if (dict.containsKey(wc)) {
                w = wc;
            } else {
                codeList.add(dict.get(w));
                if (dictSize < MAX_DICT_SIZE) {
                    dict.put(wc, dictSize++);
                }
                w = "" + c;
            }
        }

        if (!w.isEmpty()) {
            codeList.add(dict.get(w));
        }

        try (
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputPath)))
        ) {
            dos.writeInt(codeList.size());
            for (int code : codeList) {
                dos.writeShort(code);
            }
        }
    }

    public static void decompress(String inputPath, String outputPath) throws IOException {
        try (
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(inputPath)));
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputPath))
        ) {

            int codesLength = dis.readInt();
            if (codesLength <= 0) {
                throw new IOException("Codes length can't be less than 1");
            }

            int[] codes = new int[codesLength];
            for (int i = 0; i < codesLength; i++) {
                codes[i] = dis.readUnsignedShort();
            }

            Map<Integer, String> dict = new HashMap<>(512);
            for (int i = 0; i < 256; i++) {
                dict.put(i, "" + (char) i);
            }
            int dictSize = 256;

            String w = dict.get(codes[0]);
            writeString(bos, w);

            for (int i = 1; i < codes.length; i++) {
                int key = codes[i];
                String entry;

                if (dict.containsKey(key)) {
                    entry = dict.get(key);
                } else if (key == dictSize) {
                    entry = w + w.charAt(0);
                } else {
                    throw new IOException("Invalid LZW code: " + key);
                }

                writeString(bos, entry);

                if (dictSize < MAX_DICT_SIZE) {
                    dict.put(dictSize++, w + entry.charAt(0));
                }
                w = entry;
            }
        }
    }

    private static void writeString(BufferedOutputStream bos, String s) throws IOException {
        for (int i = 0, len = s.length(); i < len; i++) {
            bos.write((byte) s.charAt(i));
        }
    }
}
