
package com.kohls.google;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.*;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.math.BigInteger;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;


public class DriveServiceHelper {

    protected static final Logger log = Logger.getLogger(DriveServiceHelper.class.getName());
    protected static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String fields = "files,kind,nextPageToken";

    private Drive drive;
    private String domain;


    public DriveServiceHelper(String app_name, String dOmain, 
            String svc_acct_user, String svc_acct_email, java.io.File client_secrets) 
        throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = Authorize(svc_acct_user, svc_acct_email, client_secrets);
        this.drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(app_name)
            .build();
        this.domain = dOmain;
    }

    public GoogleCredential Authorize(String svc_acct_user, String svc_acct_email, java.io.File client_secrets)
        throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = new GoogleCredential.Builder()
            .setTransport(httpTransport)
            .setJsonFactory(JSON_FACTORY)
            .setServiceAccountId(svc_acct_email)
            .setServiceAccountUser(svc_acct_user)
            .setServiceAccountPrivateKeyFromP12File(client_secrets)
            .setServiceAccountScopes(Collections.singleton(DriveScopes.DRIVE))
            .build();
        return credential;
    }

    public List<com.google.api.services.drive.model.File> getFiles(String user_email) 
    throws IOException {
        List<com.google.api.services.drive.model.File> files = new ArrayList<com.google.api.services.drive.model.File>();
        String page_token = "";
        do {
            FileList ret = drive.files().list()
                .setQ("\"me\" in owners")
                .setCorpus("user")
                .setPageToken(page_token)
                .setFields(fields)
                .execute();
            page_token = ret.getNextPageToken();

            files.addAll(ret.getFiles());

        } while (page_token != null && page_token.length() > 0);

        return files;
    }
    public void remPermission(String fileId, String permId) 
    throws IOException {
        drive.permissions().delete(fileId, permId).execute();
    }
}
