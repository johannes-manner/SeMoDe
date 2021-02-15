package de.uniba.dsg.serverless.cli;

import java.util.List;

public interface CustomUtility {

    void start(List<String> args);

    String getName();

}
