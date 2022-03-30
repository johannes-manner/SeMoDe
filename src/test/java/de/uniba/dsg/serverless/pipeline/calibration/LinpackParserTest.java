package de.uniba.dsg.serverless.pipeline.calibration;

import de.uniba.dsg.serverless.pipeline.calibration.model.LinpackResult;
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
        LinpackResult linpackResult = parser.parseLinpack();
        assertEquals(28.218600000000002, linpackResult.getGflops());
        assertEquals(12.100999999999999, linpackResult.getExecutionTimeInS());
        assertEquals("63", linpackResult.getMachineConfig().getModelNr());
        assertEquals("Intel(R) Xeon(R) Processor @ 2.50GHz", linpackResult.getMachineConfig().getCpuModelName());
    }

    @Test
    public void testCorruptedButWorkingCase() throws SeMoDeException, URISyntaxException {
        LinpackParser parser = new LinpackParser(Paths.get(LinpackParser.class.getResource("/linpackParser/linpack_corrupted").toURI()));
        LinpackResult linpackResult = parser.parseLinpack();
        assertEquals(28.218600000000002, linpackResult.getGflops());
        assertEquals(12.100999999999999, linpackResult.getExecutionTimeInS());
    }

    @Test
    public void testOpenFaaSCorrupted() throws SeMoDeException, URISyntaxException {
        LinpackParser parser = new LinpackParser(Paths.get(LinpackParser.class.getResource("/linpackParser/linpack_openfaas").toURI()));
        LinpackResult linpackResult = parser.parseLinpack();
        assertEquals(177.4384, linpackResult.getGflops());
        assertEquals(3.7584999999999997, linpackResult.getExecutionTimeInS());
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
