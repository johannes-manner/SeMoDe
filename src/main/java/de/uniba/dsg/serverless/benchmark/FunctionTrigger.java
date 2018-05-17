package de.uniba.dsg.serverless.benchmark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

import com.google.common.io.CharStreams;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class FunctionTrigger implements Callable<String> {
	
	private final URL url;
	
	public FunctionTrigger(URL url) {
		this.url = url;
	}

	@Override
	public String call() throws SeMoDeException {
		try {
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setDoOutput(false);
			con.setRequestMethod("GET");
			con.connect();

			try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				return CharStreams.toString(in);
			}
		} catch (IOException e) {
			throw new SeMoDeException("Function execution was not possible.", e);
		}
	}

}
