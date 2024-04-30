# Spring AI demo

## Prerequisites

* Docker or Testcontainers Desktop
* Java 17

## How to run

```bash
./mvnw spring-boot:test-run
```

`Ollama` and `Qdrant` containers will be started automatically.

## How to use

```bash
http :8080/help
```

or

```bash
http :8080/help message=="How can I use Testcontainers Ollama in Java?"
```

NOTE: If the service is configured in Testcontainers Desktop, you can access to Qdrant dashboard http://localhost:6333/dashboard  
