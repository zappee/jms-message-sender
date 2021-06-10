package com.remal.jmssender.util;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.naming.Context;

import com.remal.jmssender.SimpleQueueSender;

/**
 * Java methods related to IO operations, used by this application.
 *
 * @author arnold.somogyi@gmail.com
 */
public class IoUtil {

    private static final String NO_ERROR = "";
    private static final String INDENTATION = "   ";

    /**
     * Read file and convert the content to UTF-8.
     *
     * @param out the "standard" output stream
     * @param verbose prints additional log details as to what the tool is doing
     * @param pathToFile path to the text file
     * @return file content
     * @throws IOException in case of reading file error
     */
    public static String readFile(PrintStream out, boolean verbose, String pathToFile) throws IOException {
        if (verbose) {
            out.printf("reading message from '%s' file...%n", pathToFile);
        }

        byte[] bytes = Files.readAllBytes(Paths.get(pathToFile));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Close multiply resources quietly.
     *
     * @param out the "standard" output stream
     * @param verbose prints additional log details as to what the tool is doing
     * @param context WebLogic server context
     * @param queueConnection jms queue connection
     * @param queueSession jms queue session
     */
    public static void closeResources(PrintStream out,
                                      boolean verbose,
                                      Context context,
                                      QueueConnection queueConnection,
                                      QueueSession queueSession) {
        if (verbose) {
            out.printf("%sclosing the resources...%n", AnsiColor.YELLOW);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(closeQueueSession(out, verbose, queueSession));
        sb.append(closeQueueConnection(out, verbose, queueConnection));
        sb.append(closeContext(out, verbose, context));

        String errors = sb.toString();
        if (!errors.isEmpty()) {
            out.printf(SimpleQueueSender.ERROR_MESSAGE, errors);
        }
    }

    /**
     * Close jms queue session quietly.
     *
     * @param out the "standard" output stream
     * @param verbose prints additional log details as to what the tool is doing
     * @param queueSession jms queue session
     */
    private static String closeQueueSession(PrintStream out, boolean verbose, QueueSession queueSession) {
        try {
            if (Objects.nonNull(queueSession)) {
                if (verbose) {
                    out.printf("%sclosing queue-session...%n", INDENTATION);
                }
                queueSession.close();
            }
        } catch (Exception e) {
            return "An unexpected error occurred while closing javax.jms.Context. " + e;
        }

        return NO_ERROR;
    }

    /**
     * Close jms queue connection quietly.
     *
     * @param out the "standard" output stream
     * @param verbose prints additional log details as to what the tool is doing
     * @param queueConnection jms queue connection
     */
    private static String closeQueueConnection(PrintStream out, boolean verbose, QueueConnection queueConnection) {
        try {
            if (Objects.nonNull(queueConnection)) {
                if (verbose) {
                    out.printf("%sclosing queue-connection...%n", INDENTATION);
                }
                queueConnection.close();
            }
        } catch (Exception e) {
            return "An unexpected error occurred while closing javax.jms.QueueConnection. " + e;
        }

        return NO_ERROR;
    }

    /**
     * Close WebLogic server context quietly.
     *
     * @param out the "standard" output stream
     * @param verbose prints additional log details as to what the tool is doing
     * @param context WebLogic server context
     */
    private static String closeContext(PrintStream out, boolean verbose, Context context) {
        try {
            if (Objects.nonNull(context)) {
                if (verbose) {
                    out.printf("%sclosing context...%n", INDENTATION);
                }
                context.close();
            }
        } catch (Exception e) {
            return "An unexpected error occurred while closing javax.naming.Context. " + e;
        }

        return NO_ERROR;
    }

    /**
     * Utility classes should not have a public or default constructor.
     */
    private IoUtil() {
    }
}
