package com.remal.jmssender;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.Callable;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.remal.jmssender.picocli.CustomOptionRenderer;
import com.remal.jmssender.util.IoUtil;
import com.remal.jmssender.util.AnsiColor;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * WebLogic JMS message sender command line tool.
 *
 * @author arnold.somogyi@gmail.com
 */
@Command(
        name = "JMS Message Sender",
        usageHelpWidth = 100,
        description = "JMS message sender command-line tool. This tool can send messages to the given JMS queue.%n",
        exitCodeListHeading = "%nExit codes:%n",
        exitCodeOnUsageHelp = SimpleQueueSender.USAGE_ERROR,
        exitCodeList = {
                SimpleQueueSender.NO_ERROR + ":Successful program execution.",
                SimpleQueueSender.USAGE_ERROR + ":Usage error. The user input for the command was incorrect.",
                SimpleQueueSender.RUNTIME_ERROR + ":An unexpected error appeared while executing the SQL statement." },
        footerHeading = "%nPlease report issues at arnold.somogyi@gmail.com.",
        footer = "%nDocumentation, source code: https://github.com/zappee/jms-message-sender%n")
public class SimpleQueueSender implements Callable<Integer> {

    /**
     * Application exit code used when there was no error during the execution.
     */
    public static final int NO_ERROR = 0;

    /**
     * Application exit code used when there was an mismatch in the parameters
     * provided by the user.
     */
    public static final int USAGE_ERROR = 1;

    /**
     * Application exit code used when there was an error while the execution.
     */
    public static final int RUNTIME_ERROR = 2;

    /**
     * Standard output.
     */
    private static final PrintStream OUT = System.out;

    /**
     * Error message template.
     */
    public static final String ERROR_MESSAGE = AnsiColor.RED_BOLD_BRIGHT + "%nERROR: %s" + AnsiColor.DEFAULT;

    /**
     * Definition of the general command line options.
     */
    @CommandLine.Option(
            names = {"-?", "--help"},
            usageHelp = true,
            description = "Display this help and exit.")
    private boolean help;

    @CommandLine.Option(
            names = {"-v", "--verbose"},
            description = "It provides additional details as to what the tool is doing.")
    private boolean verbose;

    @CommandLine.Option(
            names = {"-I", "--icf"},
            defaultValue = "weblogic.jndi.WLInitialContextFactory",
            description = "To create a WebLogic context from a client, your code must minimally specify this factor as"
                    + " the initial context factory. Default is '${DEFAULT-VALUE}'.")
    private String initialContextFactory;

    /**
     * WebLogic connection parameters
     */
    @CommandLine.Option(
            names = {"-T", "--protocol"},
            defaultValue = "t3",
            description = "The protocol used for connecting to the WebLogic server. Accepted values: 't3' and 'http'. "
                    + "Default is '${DEFAULT-VALUE}'.")
    private String protocol;

    @CommandLine.Option(
            names = {"-H", "--host"},
            defaultValue = "localhost",
            description = "The hostname of the machine where the WebLogic server runs. Default is '${DEFAULT-VALUE}'.")
    private String host;

    @CommandLine.Option(
            names = {"-P", "--port"},
            defaultValue = "7001",
            description = "The listening port for the WebLogic server. Default is ${DEFAULT-VALUE}.")
    private int port;

    @CommandLine.Option(
            names = {"-u", "--user"},
            defaultValue = "weblogic",
            description = "The username for the WebLogic server. Default is '${DEFAULT-VALUE}'.")
    private String user;

    /**
     * JNDI names
     */
    @CommandLine.Option(
            names = {"-c", "--cf"},
            required = true,
            description = "The JNDI name of the queue connection factory.")
    private String connectionFactoryJndi;

    @CommandLine.Option(
            names = {"-q", "--queue"},
            required = true,
            description = "The JNDI name of the queue where the message will be sent.")
    private String queueJndi;

    /**
     * A parameter group for password.
     * Password can be provided on two different ways:
     *    - via a parameter
     *    - use the interactive mode where user needs to type the password
     */
    @CommandLine.ArgGroup(multiplicity = "1",
            heading = "%nSpecify a password for the connecting user:%n")
    PasswordArgGroup passwordArgGroup;

    static class PasswordArgGroup {
        @CommandLine.Option(names = {"-p", "--password"},
                required = true,
                description = "Password for the connecting user.")
        private String password;

        @CommandLine.Option(names = {"-i", "--iPassword"},
                required = true,
                interactive = true,
                description = "Interactive way to get the password for the connecting user.")
        private String interactivePassword;
    }

    /**
     * A parameter group for message.
     * Message can be provided on two different ways:
     *    - via a parameter
     *    - from a file
     */
    @CommandLine.ArgGroup(multiplicity = "1",
            heading = "%nSpecify the message:%n")
    MessageArgGroup messageArgGroup;

    static class MessageArgGroup {
        @CommandLine.Option(names = {"-m", "--message"},
                required = true,
                description = "The message will be sent to the queue.")
        private String message;

        @CommandLine.Option(names = {"-f", "--message-fie"},
                required = true,
                description = "The path to the message file.")
        private String pathToMessageFile;
    }

    /**
     * Main program starts here.
     *
     * @param args application parameters
     */
    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new SimpleQueueSender());
        cmd.setHelpFactory(new CustomOptionRenderer());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Integer call() throws Exception {
        int exitCode = NO_ERROR;

        Context context = null;
        QueueConnection queueConnection = null;
        QueueSession queueSession = null;
        Queue queue;

        try {
            String password = Objects.isNull(passwordArgGroup.interactivePassword)
                    ? passwordArgGroup.password
                    : passwordArgGroup.interactivePassword;

            // connect to weblogic
            context = getContext(host, port, user, password);
            queueConnection = getConnectionFactory(context, connectionFactoryJndi);
            queueSession = getQueueSession(queueConnection);
            queue = getQueue(context, queueJndi);

            // send a text message
            queueConnection.start();
            String message = Objects.isNull(messageArgGroup.message)
                    ? IoUtil.readFile(OUT, verbose, messageArgGroup.pathToMessageFile)
                    : messageArgGroup.message;
            sendMessageToQueue(verbose, message, queueSession, queue);
            queueConnection.stop();

        } catch (NamingException | JMSException | IOException e) {
            String errorMessage = String.format(ERROR_MESSAGE, e.toString());
            OUT.printf(errorMessage);
            exitCode = RUNTIME_ERROR;
        } finally {
            IoUtil.closeResources(OUT, verbose, context, queueConnection, queueSession);
        }

        showExitCode(exitCode);
        return exitCode;
    }

    /**
     * Show the exit code of the application.
     *
     * @param exitCode the exit code
     */
    private void showExitCode(int exitCode) {
        String color;
        switch (exitCode) {
            case 0:
                color = AnsiColor.GREEN_BOLD_BRIGHT;
                break;

            case 1:
            case 2:
                color = AnsiColor.RED_BOLD_BRIGHT;
                break;

            default: color = AnsiColor.DEFAULT;
        }

        OUT.printf("%n");
        OUT.printf("%sReturn code: %d", color, exitCode);
        OUT.printf(AnsiColor.DEFAULT);
        OUT.printf("%n%n");
    }

    /**
     * Get WebLogic initial context.
     *
     * @param host the hostname of the machine where the WebLogic server runs
     * @param port the listening T3 port for the WebLogic server
     * @param user the username for the WebLogic server
     * @param password password for the connecting user
     * @return the WebLogic server initial context
     * @throws NamingException throw in case of error
     */
    private Context getContext(String host, int port, String user, String password) throws NamingException {
        String t3 = String.format("%s://%s:%d", protocol, host, port);
        OUT.printf(AnsiColor.YELLOW_BRIGHT);
        OUT.printf("--> getting initial context (%s, user: %s)...%n", t3, user);

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
        env.put(Context.PROVIDER_URL, t3);
        env.put(Context.SECURITY_PRINCIPAL, user);
        env.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialContext(env);
    }

    /**
     * Produce a JMS queue connection factory.
     *
     * @param context WebLogic server initial context
     * @param jndiName connection factory JNDI name
     * @return the queue connection factory
     * @throws NamingException in case of error
     * @throws JMSException throw in case error
     */
    private QueueConnection getConnectionFactory(Context context, String jndiName)
            throws NamingException, JMSException {

        OUT.printf(AnsiColor.YELLOW_BRIGHT);
        OUT.printf("--> looking up for '%s' queue connection factory...%n", jndiName);
        QueueConnectionFactory connectionFactory = (QueueConnectionFactory) context.lookup(jndiName);

        if (verbose) {
            OUT.printf(AnsiColor.YELLOW);
            OUT.printf("--> creating a queue connection...%n");
        }
        return connectionFactory.createQueueConnection();
    }

    /**
     * Create a queue session.
     *
     * @param queueConnection queue connection
     * @return the jms queue session
     * @throws JMSException throw in case of error
     */
    private QueueSession getQueueSession(QueueConnection queueConnection) throws JMSException {
        if (verbose) {
            OUT.printf(AnsiColor.YELLOW);
            OUT.printf("--> creating queue session...%n");
        }
        return queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * Get the JMS queue.
     *
     * @param context WebLogic server context
     * @param jndiName the JNDI name of the JMS queue
     * @return the queue
     * @throws NamingException throw in case of error
     */
    private Queue getQueue(Context context, String jndiName) throws NamingException {
        OUT.printf(AnsiColor.YELLOW_BRIGHT);
        OUT.printf("--> looking up for '%s' queue...%n", jndiName);
        return (Queue) context.lookup(jndiName);
    }

    /**
     * Send the given string as a TextMessage to the queue.
     *
     * @param verbose prints additional log details as to what the tool is doing
     * @param message the message as a string
     * @param queueSession jms queue session
     * @param queue queue
     * @throws JMSException throw in case of error
     */
    private static void sendMessageToQueue(boolean verbose,
                                           String message,
                                           QueueSession queueSession,
                                           Queue queue) throws JMSException {
        if (verbose) {
            OUT.printf(AnsiColor.YELLOW);
            OUT.printf("--> sending a text message to queue...%n");
        }

        TextMessage textMessage = queueSession.createTextMessage(message);

        try (QueueSender queueSender = queueSession.createSender(queue)) {
            if (verbose) {
                OUT.printf(AnsiColor.YELLOW);
                OUT.printf("--> message: '%s%s%s'%n", AnsiColor.BLUE_BRIGHT, message, AnsiColor.YELLOW);
            }

            queueSender.send(textMessage);
            OUT.printf(AnsiColor.YELLOW_BRIGHT);
            OUT.printf("--> message has been sent successfully%n");
        }
    }
}
