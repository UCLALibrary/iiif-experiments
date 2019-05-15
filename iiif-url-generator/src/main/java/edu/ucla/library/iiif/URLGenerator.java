
package edu.ucla.library.iiif;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;
import io.airlift.airline.SingleCommand;

/**
 * A throw-away command line program to generate IIIF URLs from files on disk.
 * <p>
 * To use: <code>java -jar target/iiif-url-generator-0.0.1.jar -h</code> For instance: <code>
 *   java -jar target/iiif-url-generator-0.0.1.jar -o output.csv -c input.csv
 * </code>
 * </p>
 */
@Command(name = "edu.ucla.library.iiif.URLGenerator", description = "A URLs generator")
public final class URLGenerator {

    private static final String MESSAGES = "iiif-url-generator_messages";

    private static final Logger LOGGER = LoggerFactory.getLogger(URLGenerator.class, MESSAGES);

    private static final String EOL = System.getProperty("line.separator");

    private static final String ENCODING = StandardCharsets.UTF_8.name();

    private static final int DEFAULT_TILE_SIZE = 512;

    private static final String DEFAULT_IIIF_SERVICE = "/iiif/2";

    private static final String SLASH = "/";

    @Inject
    public HelpOption myHelpOption;

    @Option(name = { "-i", "--input" }, description = "A CSV file with paths of images for which to generate URLs")
    public File myCSVFile;

    @Option(name = { "-o", "--output" }, description = "A spreadsheet of URLs")
    public String myOutput;

    @Option(name = { "-m", "--max" }, description = "A maximum number of files to process")
    public int myMaxCount;

    @Option(name = { "-e", "--extension" }, description = "A file extension for the source image")
    public String myFileExt;

    // e.g.: 110-130MB,110-130MB_JP2s,110-130MB_lossy,50-60MB,50-60MB_JP2s,50-60MB_lossy
    @Option(name = { "-p", "--prefixes" }, description = "A comma-delimited list of prefixes to append to IDs")
    public String myPrefixes;

    /**
     * The main method for the reconciler program.
     *
     * @param args Arguments supplied to the program
     */
    @SuppressWarnings("uncommentedmain")
    public static void main(final String[] args) {
        final URLGenerator generator = SingleCommand.singleCommand(URLGenerator.class).parse(args);

        if (generator.myHelpOption.showHelpIfRequested()) {
            return;
        }

        generator.run();
    }

    private void run() {
        Objects.requireNonNull(myCSVFile, LOGGER.getMessage(MessageCodes.G_001));

        try {
            final CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build();
            final CSVReader reader = new CSVReaderBuilder(new FileReader(myCSVFile)).withCSVParser(parser).build();
            final Iterator<String[]> iterator = reader.iterator();
            final FileWriter[] writers;
            final String[] prefixes;
            final int maxCount;

            LOGGER.info(MessageCodes.G_004, myCSVFile);

            int index = 0;

            // Setup a maximum number of images for which we want to generate URLs
            if (myMaxCount != 0) {
                LOGGER.info(MessageCodes.G_002, myMaxCount);
                maxCount = myMaxCount;
            } else {
                maxCount = Integer.MAX_VALUE;
            }

            // Setup the file(s) we'll write to (one per prefix or just one)
            if (myPrefixes != null) {
                LOGGER.info(MessageCodes.G_005);
                prefixes = myPrefixes.split(",");
                writers = new FileWriter[prefixes.length];

                // Open a different writer for each of our prefixes
                for (int writerIndex = 0; writerIndex < prefixes.length; writerIndex++) {
                    writers[writerIndex] = new FileWriter(new File(prefixes[writerIndex]) + "_" + myOutput);
                }
            } else {
                prefixes = new String[] { "" };
                writers = new FileWriter[] { new FileWriter(new File(myOutput)) };
            }

            // Iterate through our images, generating URLs for each
            while (iterator.hasNext()) {
                final String[] image = iterator.next();
                final String filePath;

                String id;

                if (image.length != 0) {
                    index++;

                    if (image.length == 1) {
                        filePath = image[0];

                        if (filePath.contains(SLASH)) {
                            id = filePath.substring(filePath.lastIndexOf(SLASH) + 1);
                        } else {
                            id = filePath;
                        }

                        id = FileUtils.stripExt(id) + "." + myFileExt;
                    } else {
                        filePath = image[1];
                        id = image[0];
                    }

                    for (int writerIndex = 0; writerIndex < prefixes.length; writerIndex++) {
                        final BufferedImage bimg = ImageIO.read(new File(filePath));
                        final Iterator<String> urlIterator = ImageUtils.getTilePaths(DEFAULT_IIIF_SERVICE,
                                prefixes[writerIndex] + SLASH + id, DEFAULT_TILE_SIZE, bimg.getWidth(), bimg
                                        .getHeight()).listIterator();

                        while (urlIterator.hasNext()) {
                            final String url = urlIterator.next();

                            LOGGER.debug(MessageCodes.G_006, url);

                            writers[writerIndex].write(url + EOL);
                        }
                    }
                }

                if (index >= maxCount) {
                    break;
                }
            }

            for (int writerIndex = 0; writerIndex < prefixes.length; writerIndex++) {
                writers[writerIndex].close();
            }
        } catch (final IOException details) {
            LOGGER.error(details, details.getMessage());
        }
    }
}
