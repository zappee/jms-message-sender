# Remal JMS Message Sender command line tool

_keywords: java, jms, queue, topic, message queue, weblogic, oracle, WL, bash script, shell script, command line,  tool, execute, automate, docker_

_[Release Note](release.md)_

## 1) Overview
The JMS-Sender is a flexible command-line Java tool that can be used to send text messages to any kind of JMS Queue. This is a command ine tool can be used from bash script and command line. It can be used very effectively from Windows/Linux script files.

## 2) Usage
1. Collect the connection parameter for the WebLogic server you are sending the JMS message.
1. Run the `JMS Message Sender` application.
   
   Example: `java -jar jms-sender-0.1.0-with-dependencies.jar -p weblogic12 -c jms/qcf -q jms/hello_queue -m "hello message" -v`

## 3) Help
~~~~
Usage: JMS Message Sender [-?v] -c=<connectionFactoryJndi> [-H=<host>] [-I=<initialContextFactory>]
                          [-P=<port>] -q=<queueJndi> [-u=<user>] (-p=<password> | -i) (-m=<message>
                          | -f=<pathToMessageFile>)
JMS message sender command-line tool. This tool can send messages to the given JMS queue.

  -?, --help                Display this help and exit.
  -c, --cf                  The JNDI name of the queue connection factory.
  -H, --host                The hostname of the machine where the WebLogic server runs. Default is
                              'localhost'.
  -I, --icf                 To create a WebLogic context from a client, your code must minimally
                              specify this factor as the initial context factory. Default is
                              'weblogic.jndi.WLInitialContextFactory'.
  -P, --port                The listening T3 port for the WebLogic server. Default is 7001.
  -q, --queue               The JNDI name of the queue where the message will be sent.
  -T, --protocol            The protocol used for connecting to the WebLogic server. Accepted values:
                              't3' and 'http'. Default is 't2'.
  -u, --user                The username for the WebLogic server. Default is 'weblogic'.
  -v, --verbose             It provides additional details as to what the tool is doing.

Specify a password for the connecting user:
  -i, --iPassword           Interactive way to get the password for the connecting user.
  -p, --password            Password for the connecting user.

Specify the message:
  -f, --message-fie         The path to the message file.
  -m, --message             The message will be sent to the queue.

Exit codes:
  0   Successful program execution.
  1   Usage error. The user input for the command was incorrect.
  2   An unexpected error appeared while executing the SQL statement.
~~~~

## 4) Build

1. Install the JAR into your local Maven repository.
   
   WebLogic does not provide client jar artifact in maven public repository. You must get the jar located in the `WL_HOME\server\lib` directory of your WebLogic server. This jar contains all classes needed by client.
   
   Use the following Mavan command to install the JAR into your local Maven repository:
   ~~~~
   mvn install:install-file \
      -Dfile=libraries/wlthint3client.jar \
      -DgroupId=com.oracle.weblogic \
      -DartifactId=wlthint3client \
      -Dversion=12.2.1.4.0 \
      -Dpackaging=jar
    ~~~~

1. Build the project
    ~~~~
    mvn clean package
    ~~~~

# 5) Licence
BSD (2-clause) licensed.
