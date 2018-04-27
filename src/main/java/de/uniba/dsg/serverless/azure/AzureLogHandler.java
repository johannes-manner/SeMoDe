package de.uniba.dsg.serverless.azure;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.ListFileItem;

import de.uniba.dsg.serverless.model.SeMoDeException;

public class AzureLogHandler {

	private static final Logger logger = Logger.getLogger(AzureLogHandler.class.getName());
	
	public final String storageConnectionString;
	
	public final String shareName;
	public final String functionName;
	
	public AzureLogHandler(String accountName, String accountKey, String shareName, String functionName) {
		this.shareName = shareName;
		this.functionName = functionName;
		
		this.storageConnectionString = "DefaultEndpointsProtocol=http;" +
			    "AccountName=" + accountName + ";" + 
			    "AccountKey=" + accountKey;
	}
	
	public void downloadFiles() {
		// TODO: Implement as writePerformanceDataToFile
		
		try {
		    CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			
			// Create the Azure Files client.
			CloudFileClient fileClient = storageAccount.createCloudFileClient();
			
			CloudFileShare share = fileClient.getShareReference(shareName);
			
			//Get a reference to the root directory for the share.
			CloudFileDirectory rootDir = share.getRootDirectoryReference();

			//Get a reference to the directory that contains the file
			CloudFileDirectory logDir = rootDir.getDirectoryReference("LogFiles/Application/Functions/function/" + functionName);
			
			List<CloudFile> files = getCloudFiles(logDir);
			
			for(CloudFile file : files) {
				System.out.println(file.getName());
				System.out.println(file.downloadText());
			}
			
		} catch (InvalidKeyException invalidKey) {
			invalidKey.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (StorageException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SeMoDeException e) {
			logger.severe(e.getMessage());
			logger.severe("Data handler is terminated due to an error.");
		} 
	}

	private List<CloudFile> getCloudFiles(CloudFileDirectory logDir) throws SeMoDeException {
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
