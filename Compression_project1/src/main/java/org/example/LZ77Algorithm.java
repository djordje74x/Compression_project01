package org.example;

import org.example.models.Token;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.example.util.ByteUtil.readFileToByteArray;

public class LZ77Algorithm {
    private static final int OFFSET = 255;
    private static final int FRONT_LIMIT = 15;

    public static void compress(String inputPath, String outputPath) throws IOException {
        byte[] data = readFileToByteArray(inputPath);
        List<Token> encoded = new ArrayList<>();

        int cursor = 0;
        while (cursor < data.length) {
            Token match = findMatch(data, cursor);
            encoded.add(match);
            cursor += match.getLength() + 1;
        }

        try (
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputPath)))
        ) {
            out.writeInt(encoded.size());
            for (Token token : encoded) {
                out.writeByte(token.getOffset());
                out.writeByte(token.getLength());
                out.writeByte(token.getSymbol());
            }
        }
    }

    public static void decompress(String inputPath, String outputPath) throws IOException {
        try (
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(inputPath)));
                FileOutputStream fos = new FileOutputStream(outputPath)
        ) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int tokenCount = dis.readInt();
            int offset, length, symbol;
            for (int i = 0; i < tokenCount; i++) {
                offset = dis.readUnsignedByte();
                if (offset > buffer.size()) {
                    throw new IOException("Offset exceeded limit");
                }

                length = dis.readUnsignedByte();
                symbol = dis.readUnsignedByte();

                byte[] outArr = buffer.toByteArray();
                for (int j = 0; j < length; j++) {
                    byte copied = outArr[outArr.length - offset];
                    buffer.write(copied);
                    fos.write(copied);
                }

                buffer.write(symbol);
                fos.write(symbol);
            }
        }
    }

    private static Token findMatch(byte[] input, int position) {
        int maxBack = Math.max(0, position - OFFSET), bestLen = 0, bestOff = 0, maxLen = Math.min(FRONT_LIMIT, input.length - position - 1);

        if (position == input.length - 1) {
            return new Token(0, 0, input[position] & 0xFF);
        }

        for (int i = position - 1; i >= maxBack; i--) {
            int currentLen = 0;

            while (currentLen < maxLen &&
                    (position + currentLen) < input.length - 1 &&
                    input[i + currentLen] == input[position + currentLen]) {
                currentLen++;
            }

            if (currentLen > bestLen) {
                bestLen = currentLen;
                bestOff = position - i;
                if (bestLen == maxLen) break;
            }
        }

        int nextIndex = position + bestLen;
        int nextSymbol = (nextIndex < input.length) ? (input[nextIndex] & 0xFF) : 0;

        if (bestOff > 255) {
            bestOff = 255;
        }
        if (bestLen > 255) {
            bestLen = 255;
        }

        return new Token(bestOff, bestLen, nextSymbol);
    }


}
