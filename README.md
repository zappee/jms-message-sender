## 🐧 JMS Message Sender

![GitHub top language](https://img.shields.io/github/languages/top/zappee/jms-message-sender)
![GitHub Issues](https://img.shields.io/github/issues/zappee/jms-message-sender)
![GitHub Release](https://img.shields.io/github/v/release/zappee/jms-message-sender)


### 1) Overview
The **JMS Message Sender** is a flexible, lightweight Java command-line interface (CLI) utility designed to transmit text payloads to Java Message Service (JMS) queues or topics.
It bypasses enterprise integration overhead, allowing developers to interact directly with queues via command-line prompts.
It is cross-platform and suitable for automated DevOps pipelines, shell scripts, or Docker orchestration.


### 2) Key features

* **Direct message publishing:** Directly sends text messages into targeted enterprise JMS queues.
* **Flexible payload selection:** Read raw message contents from an inline string or read payloads from a local file.
* **Header manipulation:** Inject native JMS metadata attributes like custom `Correlation-ID` parameters.
* **Secure interactive authentication:** Prompt for connection passwords dynamically at runtime rather than exposing secrets in plaintext.


### 3) Key use cases

* **Message verification:** Instantly push operational test records into messaging pipelines.
* **Automated batch processing:** Trigger script-driven data sending streams.
* **DevOps infrastructure audits:** Confirm application container access pathways to destinations like WebLogic nodes.
* **SAF communication test:** A SAF (Store-and-Forward) communication test verifies the end-to-end reliability and high availability of messages sent across distributed application servers or distinct cluster domains.


### 4) Quick Start


#### 4.1) Preparation
Collect your JMS endpoint configurations:
* hosts
* ports
* connection factory JNDI name
* targets


#### 4.2) Text message transmission

```console
$ java -jar jms-sender-0.2.2-with-dependencies.jar \
   --protocol t3 \
   --host localhost \
   --port 7001 \
   --cf jms/QueueConnectionFactory \
   --queue jms/incomingQueue \
   --user weblogic \
   --password password \
   --message "Hello wordl!" \
   --verbose
```


#### 4.3) Text message transmission using password and payload file

```console
$ java -jar jms-sender-0.2.2-with-dependencies.jar \
   -T t3 \
   -H host.domain.com \
   -P 7001 \
   -c jms/QueueConnectionFactory \
   -q jms/LogQueue \
   -u admin \
   -i \
   -f payloads/invoice_payload.json \
   -o "CORR-ID-99882"
```


### 5) Summary of exit codes

* **`0`** : Successful program execution and message publishing.
* **`1`** : Usage configuration error or incorrect user input.
* **`2`** : Unexpected internal runtime failure or connection error.


### 6) CLI Reference & Command syntax

#### 6.1) Global context

Run this command to print the comprehensive application usage guidelines, available parameters, and error exit codes:

```console
$ java -jar jms-sender-0.2.2-with-dependencies.jar --help

Usage: JMS Message Sender [-?v] -c=<connectionFactoryJndi> [-H=<host>]
                          [-I=<initialContextFactory>] [-P=<port>]
                           -q=<queueJndi> [-T=<protocol>] [-u=<user>]
                          [-o=<correlationId>] (-p=<password> | -i) (-m=<message> |
                          -f=<pathToMessageFile>)
JMS message sender command-line tool. This tool can send messages to the given JMS queue.

  -?, --help             Display this help and exit.
  -c, --cf               The JNDI name of the queue connection factory.
  -H, --host             The hostname of the machine where the WebLogic server runs. Default is
                           'localhost'.
  -I, --icf              To create a WebLogic context from a client, your code must minimally
                           specify this factor as the initial context factory. Default is 'weblogic.
                           jndi.WLInitialContextFactory'.
  -P, --port             The listening port for the WebLogic server. Default is 7001.
  -q, --queue            The JNDI name of the queue where the message will be sent.
  -T, --protocol         The protocol used for connecting to the WebLogic server. Accepted values:
                           't3' and 'http'. Default is 't3'.
  -u, --user             The username for the WebLogic server. Default is 'weblogic'.
  -v, --verbose          It provides additional details as to what the tool is doing.

JMS message header manipulation:
  -o, --correlation-id   Set the JMS Correlation ID.

Specify a password for the connecting user:
  -i, --iPassword        Interactive way to get the password for the connecting user.
  -p, --password         Password for the connecting user.

Specify the message:
  -f, --message-fie      The path to the message file.
  -m, --message          The message will be sent to the queue.

Exit codes:
  0   Successful program execution.
  1   Usage error. The user input for the command was incorrect.
  2   An unexpected error appeared while executing the SQL statement.

Please report issues at arnold.somogyi@gmail.com.
Documentation, source code: https://github.com/zappee/jms-message-sender
```


### 7) Build

1. Register required WebLogic thin-client driver locally before building the project:
```bash
$ mvn install:install-file \
  -Dfile=libraries/wlthint3client.jar \
  -DgroupId=com.oracle.weblogic \
  -DartifactId=wlthint3client \
  -Dversion=12.2.1.4.0 \
  -Dpackaging=jar
```
2. run the package command to build the artifact:
```bash
$ mvn clean package
```


### 8) Source code

[https://github.com/zappee/jms-message-sender](https://github.com/zappee/jms-message-sender)


### 🤝 Contributing

Contributions, feature requests, optimization, and bug reports are always welcome!
For more information, please visit my [homepage](https://zappee.github.io).
