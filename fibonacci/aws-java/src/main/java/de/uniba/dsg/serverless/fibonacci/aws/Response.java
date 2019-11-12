package de.uniba.dsg.serverless.fibonacci.aws;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class Response {

	private final String result;
	private final String platformId;
	private final String containerId;

	private String vmIdentification = "";
	private String cpuModel = "";
	private String cpuModelName = "";

	public Response(String result, String platformId, String containerId) {
		this.result = result;
		this.platformId = platformId;
		this.containerId = containerId;
	}

	public String getResult() {
		return this.result;
	}

	public String getPlatformId() {
		return this.platformId;
	}

	public String getContainerId() {
		return this.containerId;
	}

	public String getVmIdentification() {
		return vmIdentification;
	}

	public String getCpuModel() {
		return cpuModel;
	}

	public String getCpuModelName() {
		return cpuModelName;
	}

	public void addCPUAndVMInfo() {
		try {
			vmIdentification = Files.readAllLines(Paths.get("//proc/stat")).stream().filter(s -> s.startsWith("btime"))
					.map(s -> s.split(" ")[1]).findFirst().get();

			List<String> cpuInfo = Files.readAllLines(Paths.get("//proc/cpuinfo"));
			List<String> modelInfo = cpuInfo.stream().filter(s -> s.startsWith("model"))
					.map(s -> s.split(":")[1].trim()).collect(Collectors.toList());
			cpuModel = modelInfo.get(0);
			cpuModelName = modelInfo.get(1);

		} catch (NoSuchElementException e) {

		} catch (IOException e) {

		}
	}
}
