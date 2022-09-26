Performance tests for Energy Performance of Buildings Register services
=======================================================================

A Maven project using the [Gatling plugin for Maven](https://gatling.io/docs/current/extensions/maven_plugin/).

This project is written in Scala.

## Prerequisites

Install the following:
* Java Virtual Machine (JVM) (our CI/CD pipeline uses [Corretto 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html))
* Maven ([download here](https://maven.apache.org/download.cgi))

Set the following environment variables by adding this to your `.bashrc` (or similar):
```bash
export JAVA_HOME=/path/to/java/bin
export M2_HOME=/path/to/maven/bin
PATH=$PATH:$JAVA_HOME:$M2_HOME
```

## Running the tests

```bash
make test
```
