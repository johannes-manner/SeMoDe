package de.uniba.dsg.serverless.azure;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.ListFileItem;

import de.uniba.dsg.serverless.model.PerformanceData;
import de.uniba.dsg.serverless.model.SeMoDeException;

public class AzureLogHandler {

	private static final String FUNCTION_COMPLETED = "Function completed";

	private static final String FUNCTION_STARTED = "Function started";

	private static final Logger logger = Logger.getLogger(AzureLogHandler.class.getName());

	public final String storageConnectionString;

	public final String shareName;
	public final String functionName;

	public AzureLogHandler(String accountName, String accountKey, String shareName, String functionName) {
		this.shareName = shareName;
		this.functionName = functionName;

		this.storageConnectionString = "DefaultEndpointsProtocol=http;" + "AccountName=" + accountName + ";"
				+ "AccountKey=" + accountKey;
	}
	
	/**
	 * Retrieves the performance data from the specified cloud storage and saves it to the specified file
	 * @param fileName name of the output file
	 */
	public void writePerformanceDataToFile(String fileName) {

		try {
			List<CloudFile> logFiles = getLogFiles();
			List<PerformanceData> performanceDataList = new ArrayList<>();
			for (CloudFile file : logFiles) {
				performanceDataList.addAll(getPerformanceData(file));
			}
			if (!Files.exists(Paths.get("performanceData"))) {
				Files.createDirectory(Paths.get("performanceData"));
			}

			Path file = Files.createFile(Paths.get("performanceData/" + fileName));
			try (BufferedWriter bw = Files.newBufferedWriter(file)) {
				bw.write(PerformanceData.getCSVMetadata() + System.lineSeparator());
				for (PerformanceData performanceData : performanceDataList) {
					bw.write(performanceData.toCSVString() + System.lineSeparator());
				}
			}

		} catch (SeMoDeException e) {
			logger.severe(e.getMessage());
			logger.severe("Data handler is terminated due to an error.");
		} catch (IOException e) {
			logger.severe("IO Exception occured.");
			e.printStackTrace();
		}
	}

	private List<CloudFile> getLogFiles() throws SeMoDeException {
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

			// Create the Azure Files client.
			CloudFileClient fileClient = storageAccount.createCloudFileClient();

			CloudFileShare share = fileClient.getShareReference(shareName);

			// Get a reference to the root directory for the share.
			CloudFileDirectory rootDir = share.getRootDirectoryReference();

			// Get a reference to the directory that contains the file
			CloudFileDirectory logDir = rootDir
					.getDirectoryReference("LogFiles/Application/Functions/function/" + functionName);

			List<CloudFile> files = getFilesInDirectory(logDir);

			return files;
		} catch (InvalidKeyException | URISyntaxException | StorageException e) {
			throw new SeMoDeException(e);
		}
	}

	private List<PerformanceData> getPerformanceData(CloudFile file) throws SeMoDeException {
		String text;
		try {
			text = file.downloadText();
		} catch (StorageException | IOException e) {
			throw new SeMoDeException(e);
		}
		// Split the text into lines
		String lines[] = text.split("\\r?\\n");

		// Stores the start times of functions that are not completed
		Map<String, LocalDateTime> pendingData = new HashMap<>();

		List<PerformanceData> result = new ArrayList<>();

		for (String line : lines) {
			String[] lineParts = line.split(" ", 3);
			String message = lineParts[2];
			if (message.startsWith(FUNCTION_STARTED)) {
				// Start message
				String id = AzureLogAnalyzer.extractRequestId(message);
				LocalDateTime startTime = AzureLogAnalyzer.parseTime(lineParts[0]);
				pendingData.put(id, startTime);
			} else if (message.startsWith(FUNCTION_COMPLETED)) {
				// Finish Message
				String id = AzureLogAnalyzer.extractRequestId(message);
				LocalDateTime startTime = pendingData.remove(id);
				double duration = AzureLogAnalyzer.extractDuration(message);
				PerformanceData data = new PerformanceData(functionName, "", id, startTime, duration, -1, -1, -1);
				result.add(data);
			} else {
				// First line or unknown lines
				continue;
			}
		}
		return result;
	}


	private List<CloudFile> getFilesInDirectory(CloudFileDirectory logDir) throws SeMoDeException {
		ArrayList<CloudFile> cloudFiles = new ArrayList<>();

		for (ListFileItem fileItem : logDir.listFilesAndDirectories()) {
			URI fileURI = fileItem.getUri();
			String fileName = Paths.get(fileURI.getPath()).getFileName().toString();
			CloudFile file;
			try {
				file = logDir.getFileReference(fileName);
			} catch (URISyntaxException | StorageException e) {
				throw new SeMoDeException("Exception while loading file from Azure", e);
			}
			cloudFiles.add(file);
		}

		return cloudFiles;
	}

}
