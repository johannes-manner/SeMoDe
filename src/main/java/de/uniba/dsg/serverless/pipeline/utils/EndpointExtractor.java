package de.uniba.dsg.serverless.pipeline.utils;

import de.uniba.dsg.serverless.model.SeMoDeException;
import de.uniba.dsg.serverless.pipeline.model.config.LanguageConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class EndpointExtractor {

    // Properties specific to our bash scripts
    private static final String SERVICE_ESCAPE = "Deploy function: ";

    // Properties specific to serverless
    private static final String ENDPOINT_URL_SERVERLESS = "  GET - ";

    // Properties specific to maven
    private static final String ENDPOINT_URL_MAVEN = "[INFO] Successfully deployed Azure Functions at ";

    // Properties specific to azure js (kudu)
    private static final String AZURE_JS_SERVICE_NAME = "Serverless: Creating function app: ";
    private static final String AZURE_JS_UPLOAD = "Serverless: Uploading function: ";

    // Properties specific to google
    private static final String POST_URL = "POST ";

    private final Map<String, LanguageConfig> languageConfigs;
    private final Path deploymentFolderPath;
    private final Path outputDir;

    public EndpointExtractor(final Map<String, LanguageConfig> languageConfigs, final Path deploymentFolderPath, final Path outputDir) {
        this.languageConfigs = languageConfigs;
        this.deploymentFolderPath = deploymentFolderPath;
        this.outputDir = outputDir;
    }

    public void extractEndpoints(final String language, final String provider) throws SeMoDeException {

        final String providerLanguage = provider + "-" + language;
        final String apiSuffix = "/api/fibonacci-" + language;

        final Path endpointFile = Paths.get(this.outputDir.toString(), providerLanguage);
        final Path inputFilePath = Paths.get(this.deploymentFolderPath.toString(), providerLanguage + "-deploy");

        try (final BufferedWriter writer = Files.newBufferedWriter(endpointFile, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            try (final BufferedReader reader = Files.newBufferedReader(inputFilePath)) {
                String line = reader.readLine();
                String url;
                final String logType = this.languageConfigs.get(providerLanguage).getLogType();
                String currentFunction = "";
                while (line != null) {

                    if (line.startsWith(SERVICE_ESCAPE)) {
                        currentFunction = line.substring(SERVICE_ESCAPE.length());
                        writer.write(currentFunction);
                        writer.write(" ");
                    }

                    switch (logType) {
                        case "maven":
                            if (line.startsWith(ENDPOINT_URL_MAVEN)) {
                                url = line.substring(ENDPOINT_URL_MAVEN.length());
                                writer.write(url + apiSuffix);
                                writer.newLine();
                            }
                            break;
                        case "serverless":
                            if (line.contains(ENDPOINT_URL_SERVERLESS)) {
                                writer.write(line.substring(ENDPOINT_URL_SERVERLESS.length()));
                                writer.newLine();
                            }
                            break;
                        case "kudu":
                            if (line.startsWith(AZURE_JS_SERVICE_NAME)) {
                                writer.write("https://" + line.substring(AZURE_JS_SERVICE_NAME.length())
                                        + ".azurewebsites.net/api/");
                            } else if (line.startsWith(AZURE_JS_UPLOAD)) {
                                writer.write(line.substring(AZURE_JS_UPLOAD.length()));
                                writer.newLine();
                            }
                            break;
                        case "post":
                            if (line.startsWith(POST_URL)) {
                                if (line.contains(currentFunction)) {
                                    writer.write(line.substring(POST_URL.length()).split(" ")[0]);
                                    writer.newLine();
                                }
                            }
                    }
                    line = reader.readLine();
                }
            }
        } catch (final IOException e) {
            throw new SeMoDeException("Something went wrong during file operations. " + e.getMessage(), e);
        }
    }
}
