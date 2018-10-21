package de.stonedroid.vertretungsplan;

import java.io.*;

/**
 * Intern class with handy functions
 */
final class Utils
{
    /**
     * Converts an InputStream into a UTF-8 encoded string
     *
     * @param stream InputStream to convert
     * @return Converted UTF-8 string from stream
     * @throws IOException Couldn't convert InputStream
     */
    public static String inputStreamToString(InputStream stream) throws IOException
    {
        // OutputStream containing our result
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        // Byte buffer, which stores 1024 bytes of the input stream at one moment.
        byte[] buffer = new byte[1024];
        // Contains the number of bytes which are actually read.
        int len;

        // Read as long as stream.read() doesn't return -1
        // If stream.read() is -1, EOF was reached
        while ((len = stream.read(buffer)) != -1)
        {
            // Writing bytes directly to OutputStream with an offset of 0
            result.write(buffer, 0, len);
        }

        // Convert OutputStream to a UTF-8 based string
        return result.toString("utf-8");
    }

    /**
     * Reads the given file until it reaches the EOF and suddenly returns the result;
     *
     * @param file File to read
     * @return Whole file content
     * @throws IOException Couldn't read file to the end
     */
    public static String readFileToEnd(String file) throws IOException
    {
        // Read whole file using BufferedReader to read line by line
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        StringBuilder sb = new StringBuilder();

        while ((line = reader.readLine()) != null)
        {
            sb.append(line).append("\n");
        }

        // Close reader
        reader.close();
        // Remove last "\n"
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
