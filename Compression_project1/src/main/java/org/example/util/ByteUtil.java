package org.example.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class ByteUtil {

    public static byte[] readFileToByteArray(String path) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = bis.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        }
    }
}
