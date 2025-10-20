package org.example;

import org.example.models.FrequencyTable;
import org.example.models.Symbol;

import java.io.*;
import java.util.*;

import static org.example.util.EntropyCalculator.calculateFrequency;

public class ShannonFanoAlgorithm {
    public static void compress(String inputPath, String outputPath) throws IOException {
        FrequencyTable frequencyTable = calculateFrequency(new File(inputPath));
        List<Symbol> symbols = collectSymbols(frequencyTable);

        symbols.sort((a, b) -> Long.compare(b.getFrequency(), a.getFrequency()));

        Map<Integer, String> codeMap = new HashMap<>();
        generateCodes(symbols, codeMap, "");

        try (
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputPath));
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputPath)))
        ) {

            dos.writeInt(codeMap.size());
            for (Map.Entry<Integer, String> entry : codeMap.entrySet()) {
                dos.writeByte(entry.getKey());
                dos.writeUTF(entry.getValue());
            }

            StringBuilder encodedBits = new StringBuilder();
            int dataByte;
            while ((dataByte = bis.read()) != -1) {
                encodedBits.append(codeMap.get(dataByte & 0xFF));
            }

            dos.writeInt(encodedBits.length());

            int buffer = 0;
            int bitCounter = 0;
            for (int i = 0; i < encodedBits.length(); i++) {
                buffer = (buffer << 1) | (encodedBits.charAt(i) == '1' ? 1 : 0);
                bitCounter++;

                if (bitCounter == 8) {
                    dos.writeByte(buffer);
                    buffer = 0;
                    bitCounter = 0;
                }
            }

            if (bitCounter > 0) {
                buffer <<= (8 - bitCounter);
                dos.writeByte(buffer);
            }
        }
    }

    public static void decompress(String inputFile, String outputFile) throws IOException {
        try (
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))
        ) {

            int codeCount = dis.readInt();
            if (codeCount <= 0) {
                return;
            }

            Map<String, Integer> reverseCodeMap = new HashMap<>(codeCount);
            for (int i = 0; i < codeCount; i++) {
                int symbol = dis.readUnsignedByte();
                String code = dis.readUTF();
                reverseCodeMap.put(code, symbol);
            }

            int total = dis.readInt();
            StringBuilder currentCode = new StringBuilder();

            int bits = 0;
            while (bits < total) {
                int oneByte = dis.readUnsignedByte();
                for (int j = 7; j >= 0 && bits < total; j--) {
                    int bit = (oneByte >> j) & 1;
                    currentCode.append(bit == 1 ? '1' : '0');

                    Integer symbol = reverseCodeMap.get(currentCode.toString());
                    if (symbol != null) {
                        bos.write(symbol);
                        currentCode.setLength(0);
                    }
                    bits++;
                }
            }
        }
    }

    private static List<Symbol> collectSymbols(FrequencyTable freqTable) {
        List<Symbol> symbols = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            long freq = freqTable.getCountForSymbol(i);
            if (freq > 0) symbols.add(new Symbol(i, freq));
        }
        return symbols;
    }

    private static void generateCodes(List<Symbol> symbols, Map<Integer, String> codeMap, String prefix) {
        if (symbols.size() == 1) {
            Symbol s = symbols.get(0);
            codeMap.put(s.getValue(), prefix.isEmpty() ? "0" : prefix);
            return;
        }

        int split = findSplitIndex(symbols);
        List<Symbol> left = new ArrayList<>(symbols.subList(0, split));
        List<Symbol> right = new ArrayList<>(symbols.subList(split, symbols.size()));

        generateCodes(left, codeMap, prefix + "0");
        generateCodes(right, codeMap, prefix + "1");
    }

    private static int findSplitIndex(List<Symbol> symbols) {
        long totalFreq = 0;
        for (Symbol symbol : symbols) {
            totalFreq += symbol.getFrequency();
        }

        long half = totalFreq / 2;
        long acc = 0;
        for (int i = 0; i < symbols.size(); i++) {
            acc += symbols.get(i).getFrequency();
            if (acc >= half) {
                return i + 1;
            }
        }
        return symbols.size();
    }
}
