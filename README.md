Performance tests for Energy Performance of Buildings Register services
=======================================================================

A Maven project using the [Gatling plugin for Maven](https://gatling.io/docs/current/extensions/maven_plugin/).

This project is written in Scala.

## Prerequisites

Install the following:
* Java Virtual Machine (JVM) (our CI/CD pipeline uses [Corretto 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)). Scroll down the table to find the macOS 64 platform link (it may not look scrollable but it is). Once downloaded double click to install.
* Maven ([download here](https://maven.apache.org/download.cgi))

Set the following environment variables by adding this to your `.bashrc` (or similar):
```bash
export JAVA_HOME=/path/to/java/bin
or
export JAVA_HOME=$(/usr/libexec/java_home) # this eval always picks the latest java export if you installed it as suggested above

export M2_HOME=/path/to/maven/bin
PATH=$PATH:$JAVA_HOME:$M2_HOME
```

## Running the tests

To run the performance test

```bash
make performance-test-staging
```
