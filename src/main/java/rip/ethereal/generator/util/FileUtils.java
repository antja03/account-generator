package rip.ethereal.generator.util;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author antja03
 */
public class FileUtils {

    /**
     * Extracts a directory/file from the classpath/the running jar file to the given location
     *
     * @param source The directory/file that you're trying to extract from the classpath (starts from within the jar
     *               not the absolute path)
     * @param destination The absolute path on the pc that your trying to extract the file to
     * @throws IOException
     */
    public static void extractFromClasspath(String source, String destination) throws IOException {
        new File(destination).mkdirs();

        final URL dirURL = FileUtils.class.getResource(source);
        final String path = source.substring(1);

        if (dirURL == null) {
            throw new IllegalStateException(String.format("Can't find %s on the classpath.", source));
        }

        if (!dirURL.getProtocol().equals("jar")) {
            throw new IllegalStateException(String.format("%s is not located in a jar file... aborting.", source));
        }

        final JarURLConnection jarConnection = (JarURLConnection) dirURL.openConnection();
        final ZipFile jar = jarConnection.getJarFile();
        final Enumeration<? extends ZipEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            final String name = entry.getName();

            if (!name.startsWith(path))
                continue;

            final String entryTail = name.substring(path.length());
            final File file = new File(destination + File.separator + entryTail);

            if (entry.isDirectory()) {
                final boolean directoryMade = file.mkdir();
                if (!directoryMade) {
                    System.out.println("Unable to create the directory... aborting.");
                }
            } else {
                final InputStream inputStream = jar.getInputStream(entry);
                final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                final byte buffer[] = new byte[4096];

                int readCount;
                while ((readCount = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, readCount);
                }

                outputStream.close();
                inputStream.close();
            }
        }
    }
}
