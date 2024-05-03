package com.example.demogenai;

import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

@TestConfiguration(proxyBeanMethods = false)
public class IngestionConfiguration {

    @Value("classpath:/docs/java-modules.txt")
    private Resource javaModules;

    @Value("classpath:/docs/golang-modules.txt")
    private Resource goModules;

    @Value("classpath:/docs/java-ollama.pdf")
    private Resource ollamaJavaModule;

    @Value("classpath:/docs/go-ollama.pdf")
    private Resource ollamaGoModule;

    @Bean
    ApplicationRunner init(VectorStore vectorStore) {
        return args -> {
            var javaTextReader = new TextReader(this.javaModules);
            javaTextReader.getCustomMetadata().put("language", "java");
            var goTextReader = new TextReader(this.goModules);
            goTextReader.getCustomMetadata().put("language", new String[]{"go", "golang"});

            var tokenTextSplitter = new TokenTextSplitter();
            var javaDocuments = tokenTextSplitter.apply(javaTextReader.get());
            var goDocuments = tokenTextSplitter.apply(goTextReader.get());

            var pdfDocumentReaderConfig = PdfDocumentReaderConfig.builder()
                    .withPageExtractedTextFormatter(
                            new ExtractedTextFormatter.Builder().build()
                    )
                    .build();

            var ollamaJavaDocuments = tokenTextSplitter.apply(new PagePdfDocumentReader(this.ollamaJavaModule, pdfDocumentReaderConfig).get())
                    .stream()
                    .map(doc -> {
                        doc.getMetadata().put("language", "java");
                        doc.getMetadata().put("category", "module");
                        doc.getMetadata().put("project", "testcontainers");
                        doc.getMetadata().put("module", "ollama");
                        return doc;
                    })
                    .toList();
            var ollamaGoDocuments = tokenTextSplitter.apply(new PagePdfDocumentReader(this.ollamaGoModule, pdfDocumentReaderConfig).get())
                    .stream()
                    .map(doc -> {
                        doc.getMetadata().put("language", new String[]{"go", "golang"});
                        doc.getMetadata().put("category", "module");
                        doc.getMetadata().put("project", "testcontainers");
                        doc.getMetadata().put("module", "ollama");
                        return doc;
                    })
                    .toList();

            vectorStore.add(javaDocuments);
            vectorStore.add(goDocuments);
            vectorStore.add(ollamaJavaDocuments);
            vectorStore.add(ollamaGoDocuments);
        };
    }
}
