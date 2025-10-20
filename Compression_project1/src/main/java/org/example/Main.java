package org.example;

import org.example.util.EntropyCalculator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {

    private static PrintWriter report;

    public static void main(String[] args) {
        String inputBinary = (args.length > 0) ? args[0] : "sample.bin";
        String inputText = (args.length > 1) ? args[1] : "sample.txt";
        String reportPath = "report.txt";

        try {
            report = new PrintWriter(reportPath);

            double entropyBin = EntropyCalculator.calculateEntropy(Path.of(inputBinary).toFile());
            double entropyTxt = EntropyCalculator.calculateEntropy(Path.of(inputText).toFile());

            section("ENTROPIJA");
            logFormatted("Datoteka: %-12s Entropija: %.5f bits/symbol%n", inputBinary, entropyBin);
            logFormatted("Datoteka: %-12s Entropija: %.5f bits/symbol%n%n", inputText, entropyTxt);

            processAlgorithmGroup("HUFFMAN KODIRANJE", inputBinary, inputText, ".huff");
            processAlgorithmGroup("SHANNON–FANO METODA", inputBinary, inputText, ".sf");
            processAlgorithmGroup("LZ77 KOMPRESIJA", inputBinary, inputText, ".lz77");
            processAlgorithmGroup("LZW KOMPRESIJA", inputBinary, inputText, ".lzw");

            report.close();

            System.out.println("Izvestaj sacuvan u fajl: " + reportPath);

        } catch (IOException e) {
            System.err.println("Greska prilikom generisanja izvestaja: " + e.getMessage());
        }
    }

    private static void processAlgorithmGroup(String title, String file1, String file2, String extension) throws IOException {
        section(title);
        analyzeFile(file1, extension);
        analyzeFile(file2, extension);
        report.println();
    }

    private static void analyzeFile(String input, String ext) throws IOException {
        String compressed = input + ext;
        String decompressed = input + ext + ".dec";

        long originalSize = Files.size(Path.of(input));

        long startComp = System.nanoTime();
        runCompression(ext, input, compressed);
        long endComp = System.nanoTime();

        long compressedSize = Files.size(Path.of(compressed));

        long startDecomp = System.nanoTime();
        runDecompression(ext, compressed, decompressed);
        long endDecomp = System.nanoTime();

        writeReportBlock(input, originalSize, compressedSize, endComp - startComp, endDecomp - startDecomp, decompressed);
    }

    private static void runCompression(String ext, String in, String out) throws IOException {
        switch (ext) {
            case ".huff" -> HuffmanAlgorithm.compress(in, out);
            case ".sf" -> ShannonFanoAlgorithm.compress(in, out);
            case ".lz77" -> LZ77Algorithm.compress(in, out);
            case ".lzw" -> LZWAlgorithm.compress(in, out);
        }
    }

    private static void runDecompression(String ext, String in, String out) throws IOException {
        switch (ext) {
            case ".huff" -> HuffmanAlgorithm.decompress(in, out);
            case ".sf" -> ShannonFanoAlgorithm.decompress(in, out);
            case ".lz77" -> LZ77Algorithm.decompress(in, out);
            case ".lzw" -> LZWAlgorithm.decompress(in, out);
        }
    }

    private static void writeReportBlock(String fileName, long inSize, long compSize,
                                         long compTime, long decompTime, String decompFile) throws IOException {

        double ratio = (double) compSize / inSize;
        boolean identical = filesMatch(fileName, decompFile);

        logFormatted("Datoteka: %s%n", fileName);
        logFormatted("Originalna velicina : %d bajtova%n", inSize);
        logFormatted("Kompresovana velicina: %d bajtova%n", compSize);
        logFormatted("Kompresioni odnos   : %.4f%n", ratio);
        logFormatted("Vreme kompresije    : %.2f ms%n", compTime / 1e6);
        logFormatted("Vreme dekompresije  : %.2f ms%n", decompTime / 1e6);
        logFormatted("Integritet fajla    : %s%n", identical ? "podaci identicni" : "podaci se razlikuju");
        logLine();
    }

    private static boolean filesMatch(String f1, String f2) throws IOException {
        return Arrays.equals(Files.readAllBytes(Path.of(f1)), Files.readAllBytes(Path.of(f2)));
    }

    private static void section(String title) {
        String line = "-".repeat(60);
        System.out.println("\n" + title);
        System.out.println(line);
        report.printf("%n%s%n%s%n", title, line);
    }

    private static void logFormatted(String fmt, Object... args) {
        System.out.printf(fmt, args);
        report.printf(fmt, args);
    }

    private static void logLine() {
        System.out.println("------------------------------------------------------------");
        report.println("------------------------------------------------------------");
    }
}
