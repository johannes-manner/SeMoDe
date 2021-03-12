package de.uniba.dsg.serverless.pipeline.calibration;

import de.uniba.dsg.serverless.pipeline.calibration.model.GflopsExecutionTime;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinpackParserTest {

    @Test
    public void testGoodCase() throws URISyntaxException, SeMoDeException {
        LinpackParser parser = new LinpackParser(Paths.get(LinpackParser.class.getResource("/linpackParser/linpack").toURI()));
        GflopsExecutionTime gflopsExecutionTime = parser.parseLinpack();
        assertEquals(28.218600000000002, gflopsExecutionTime.getGflops());
        assertEquals(12.100999999999999, gflopsExecutionTime.getExecutionTimeInS());
    }

    @Test
    public void testCorruptedButWorkingCase() throws SeMoDeException, URISyntaxException {
        LinpackParser parser = new LinpackParser(Paths.get(LinpackParser.class.getResource("/linpackParser/linpack_corrupted").toURI()));
        GflopsExecutionTime gflopsExecutionTime = parser.parseLinpack();
        assertEquals(28.218600000000002, gflopsExecutionTime.getGflops());
        assertEquals(12.100999999999999, gflopsExecutionTime.getExecutionTimeInS());
    }

    @Test
    public void testCompletelyCorruptedCase() throws URISyntaxException {
        LinpackParser parser = new LinpackParser(Paths.get(LinpackParser.class.getResource("/linpackParser/linpack_corrupted_completely").toURI()));
        assertThrows(SeMoDeException.class, () -> parser.parseLinpack());
    }

    @Test
    public void fileNotThere() throws URISyntaxException {
        LinpackParser parser = new LinpackParser(Paths.get("/linpackParser/file_not_there"));
        assertThrows(SeMoDeException.class, () -> parser.parseLinpack());
    }
}
