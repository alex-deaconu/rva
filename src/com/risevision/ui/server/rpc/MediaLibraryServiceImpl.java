package com.risevision.ui.server.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.google.api.client.auth.security.PrivateKeys;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.Base64;
import com.google.api.client.util.StringUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.risevision.ui.client.common.exception.ServiceFailedException;
import com.risevision.ui.client.common.info.MediaItemInfo;
import com.risevision.ui.client.common.info.MediaItemsInfo;
import com.risevision.ui.client.common.service.MediaLibraryService;
import com.risevision.ui.server.amazonImpl.ListBucketResponse;
import com.risevision.ui.server.gcs.AppIdentityCredential;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class MediaLibraryServiceImpl extends RemoteServiceServlet implements MediaLibraryService {
	
	/** Global configuration of Google Cloud Storage OAuth 2.0 scope. */
	private static final String STORAGE_SCOPE = "https://www.googleapis.com/auth/devstorage.read_write";

	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	
	private static final String PROJECT_ID = "452091732215";
    
	protected static final Logger log = Logger.getAnonymousLogger();

	
	public MediaItemsInfo getBucketItems(String bucketName) throws ServiceFailedException {
		try {
			AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(STORAGE_SCOPE));

//			bucketName = "first-bucket-test";
			String URI = MediaItemInfo.MEDIA_LIBRARY_URL + bucketName;
			HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
			
			GenericUrl url = new GenericUrl(URI);
			HttpRequest request = requestFactory.buildGetRequest(url);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("x-goog-project-id", PROJECT_ID);
			
			request.setHeaders(headers);
			HttpResponse response = request.execute();

//			log.warning(response.parseAsString());

			ListBucketResponse listBucketResponse = new ListBucketResponse(response.getContent());
			
			MediaItemsInfo mediaItemsInfo = new MediaItemsInfo();
			mediaItemsInfo.setMediaItems((ArrayList<MediaItemInfo>) listBucketResponse.entries);
			
			return mediaItemsInfo;
		} catch (HttpResponseException e) {
			log.warning(e.getStatusCode() + " - " + e.getMessage());
			
			throw new ServiceFailedException(ServiceFailedException.NOT_FOUND);
		} catch (IOException e) {
			log.severe("Error - " + e.getMessage());
		}

		return null;
	}
	
	public void createBucket(String bucketName) throws ServiceFailedException {
		try {
			log.info("Creating bucket");

			AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(STORAGE_SCOPE));
	
			String URI = MediaItemInfo.MEDIA_LIBRARY_URL + bucketName;
			HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
			
			GenericUrl url = new GenericUrl(URI);
			HttpRequest request = requestFactory.buildPutRequest(url, null);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("x-goog-project-id", PROJECT_ID);
			
			request.setHeaders(headers);

			request.execute();
			
		} catch (HttpResponseException e) {
			log.warning(e.getStatusCode() + " - " + e.getMessage());
			
			throw new ServiceFailedException(ServiceFailedException.NOT_FOUND);
		} catch (IOException e) {
			log.severe("Error - " + e.getMessage());
		}

	}
	
	public void deleteMediaItem(String bucketName, String itemName) throws ServiceFailedException {
		try {
			AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(STORAGE_SCOPE));
	
			itemName = URLEncoder.encode(itemName, "UTF-8");
			String URI = MediaItemInfo.MEDIA_LIBRARY_URL + bucketName + "/" + itemName;
			HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
			
			GenericUrl url = new GenericUrl(URI);
			HttpRequest request = requestFactory.buildDeleteRequest(url);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("x-goog-project-id", PROJECT_ID);
			
			request.setHeaders(headers);

			request.execute();
			
		} catch (HttpResponseException e) {
			log.warning(e.getStatusCode() + " - " + e.getMessage());
			
			throw new ServiceFailedException(ServiceFailedException.NOT_FOUND);
		} catch (IOException e) {
			log.severe("Error - " + e.getMessage());
		}

	}
	
	public void deleteMediaItems(String bucketName, ArrayList<String> itemNames) throws ServiceFailedException {
		for (String itemName : itemNames) {
			deleteMediaItem(bucketName, itemName);
		}
	}
	
	public String getSignedPolicy(String policyBase64) {		
		PrivateKey privateKey = null;
		try {
			privateKey = setServiceAccountPrivateKeyFromP12File();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    byte[] contentBytes = StringUtils.getBytesUtf8(policyBase64);
	    try {
		    Signature signer = Signature.getInstance("SHA256withRSA");
		    signer.initSign(privateKey);
		    signer.update(contentBytes);
		    byte[] signature = signer.sign();
		    return Base64.encodeBase64String(signature);
	    } catch (GeneralSecurityException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
	    }
	    
	    return null;
	}
	
	/**
	 * Sets the private key to use with the the service account flow or
	 * {@code null} for none.
	 * 
	 * <p>
	 * Overriding is only supported for the purpose of calling the super
	 * implementation and changing the return type, but nothing else.
	 * </p>
	 * 
	 * @param p12File
	 *            input stream to the p12 file (closed at the end of this method
	 *            in a finally block)
	 */
	public PrivateKey setServiceAccountPrivateKeyFromP12File()
			throws GeneralSecurityException, IOException {
		String p12FileName = "/WEB-INF/key/65bd1c5e62dadd4852c8b04bf5124749985e8ff8-privatekey.p12";
		
		ServletContext context = getServletContext();
		InputStream is = context.getResourceAsStream(p12FileName);
		
		PrivateKey serviceAccountPrivateKey = PrivateKeys.loadFromKeyStore(KeyStore.getInstance("PKCS12"), is, "notasecret", "privatekey", "notasecret");

		return serviceAccountPrivateKey;
	}
}
