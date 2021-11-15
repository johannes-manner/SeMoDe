package de.uniba.dsg.serverless.java.aws;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class Response {

	private final boolean cold;
	private final String result;
	private final String platformId;
	private final String containerId;

	private String vmIdentification = "";
	private String cpuModel = "";
	private String cpuModelName = "";

	public Response(boolean cold, String result, String platformId, String containerId) {
		this.cold = cold;
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

	public boolean isCold() {
		return cold;
	}

	public void addCPUAndVMInfo() {
		try {
			vmIdentification = Files.readAllLines(Paths.get("//proc/self/cgroup")).stream().filter(s -> s.contains("sandbox-root-"))
					.map(s -> (s.split("sandbox-root-")[1]).substring(0,6)).findFirst().get();

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
