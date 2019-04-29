
package edu.ucla.library.iiiftest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.I18nRuntimeException;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import edu.ucla.library.s3image.MessageCodes;
import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;
import io.airlift.airline.SingleCommand;

/**
 * A throw-away command line program to upload images, with IDs, into an S3 bucket.
 * <p>
 * To use: <code>java -jar target/iiif-test-prep-0.0.1.jar -h</code> For instance: <code>
 *   java -jar target/iiif-test-prep-0.0.1.jar -b /tmp/file_dir -m 1000 -c input.csv
 * </code>
 * </p>
 */
@Command(name = "edu.ucla.library.iiiftest.TestPrep", description = "A testing preparer")
public final class TestPrep {

    private static final String MESSAGES = "iiif-testing-prep_messages";

    private static final String TIFF_EXT = ".tif";

    private static final Logger LOGGER = LoggerFactory.getLogger(TestPrep.class, MESSAGES);

    @Inject
    public HelpOption myHelpOption;

    @Option(name = { "-c", "--csv" }, description = "A CSV file with IDs and paths of images to prepare")
    public File myCSVFile;

    @Option(name = { "-o", "--outputdir" }, description = "A directory into which to write the selected files")
    public File myDestination;

    @Option(name = { "-m", "--max" }, description = "A maximum number of files to process")
    public int myMaxCount;

    @Option(name = { "-i", "--ignoredups" }, description = "Whether duplicates should just be ignored")
    public boolean isIgnoringDups;

    @Option(name = { "-d", "--dryrun" }, description = "Is just a dry run, don't actually move files")
    public boolean isDryRun;

    /**
     * The main method for the reconciler program.
     *
     * @param args Arguments supplied to the program
     */
    @SuppressWarnings("uncommentedmain")
    public static void main(final String[] args) {
        final TestPrep testPrep = SingleCommand.singleCommand(TestPrep.class).parse(args);

        if (testPrep.myHelpOption.showHelpIfRequested()) {
            return;
        }

        testPrep.run();
    }

    private void run() {
        Objects.requireNonNull(myCSVFile, LOGGER.getMessage(MessageCodes.T_001));
        Objects.requireNonNull(myDestination, LOGGER.getMessage(MessageCodes.T_002));

        try {
            final CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build();
            final CSVReader reader = new CSVReaderBuilder(new FileReader(myCSVFile)).withCSVParser(parser).build();
            final Iterator<String[]> iterator = reader.iterator();
            final List<String> filePaths = new ArrayList();
            final Map<String, String> files = new HashMap();
            final Map<String, String> ids = new HashMap();

            LOGGER.info(MessageCodes.T_004, myCSVFile);

            // If we have an ID, set it and use the file system path for the image's S3 path
            // If we don't have an ID, set the file name as the ID and use that as the S3 path
            while (iterator.hasNext()) {
                final String[] image = iterator.next();
                final String[] parts;
                final String oldPath;
                final String path;
                final String id;

                if (image.length == 2) {
                    id = image[0] + TIFF_EXT;
                    path = image[1];
                } else if (image.length == 1) {
                    path = image[0];
                    parts = path.split("/");
                    id = parts[parts.length - 1];
                } else {
                    throw new I18nRuntimeException(MESSAGES, MessageCodes.T_008, StringUtils.toString(image, '|'));
                }

                if (!filePaths.add(path)) {
                    throw new I18nRuntimeException(MESSAGES, MessageCodes.T_006, path);
                }

                if (files.put(path, id) != null && !isIgnoringDups) {
                    throw new I18nRuntimeException(MESSAGES, MessageCodes.T_007, id);
                }

                if ((oldPath = ids.put(id, path)) != null && !isIgnoringDups) {
                    throw new I18nRuntimeException(MESSAGES, MessageCodes.T_005, id, path, oldPath);
                }
            }

            // Randomize our data to get a smaller sample
            Collections.shuffle(filePaths);

            // Pull just the sample size we want
            for (int index = 0; index < myMaxCount; index++) {
                final String path = filePaths.get(index);
                final File file = new File(myDestination, files.get(path));

                if (isDryRun) {
                    LOGGER.info(MessageCodes.T_009, path, file);
                } else {
                    LOGGER.info(MessageCodes.T_010, path, file);
                    FileUtils.copy(new File(path), file);
                }
            }
        } catch (final IOException details) {
            LOGGER.error(details, details.getMessage());
        }
    }

}
