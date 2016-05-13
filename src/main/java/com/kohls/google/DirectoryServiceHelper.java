package com.kohls.google;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.*;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.math.BigInteger;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.DirectoryScopes;

import com.google.api.services.admin.directory.model.OrgUnit;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.Users;
import com.google.api.services.admin.directory.model.UserName;
import com.google.api.services.admin.directory.Directory;


public class DirectoryServiceHelper {

    protected static final Logger log = Logger.getLogger(DirectoryServiceHelper.class.getName());
    protected static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private Directory directory;
    private String domain;


    public DirectoryServiceHelper(String app_name, String dOmain, 
            String svc_acct_user, String svc_acct_email, File client_secrets) 
        throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = Authorize(svc_acct_user, svc_acct_email, client_secrets);
        this.directory = new Directory.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(app_name)
            .build();
        this.domain = dOmain;
    }

    public GoogleCredential Authorize(String svc_acct_user, String svc_acct_email, File client_secrets)
        throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = new GoogleCredential.Builder()
            .setTransport(httpTransport)
            .setJsonFactory(JSON_FACTORY)
            .setServiceAccountId(svc_acct_email)
            .setServiceAccountUser(svc_acct_user)
            .setServiceAccountPrivateKeyFromP12File(client_secrets)
            .setServiceAccountScopes(Collections.singleton(DirectoryScopes.ADMIN_DIRECTORY_USER))
            .build();
        return credential;
    }

    public List<User> getUsers() throws IOException {
        List<User> users = new ArrayList<User>();
        String page_token = "";
        do {
            // TODO - TEST flag
            if (users.size() > 1000)
                return users;
            Users ret = directory.users().list()
                .setDomain(domain)
                .setPageToken(page_token)
                .setMaxResults(500)
                .setQuery("email=michael.osiecki@gtest.kohls.com")
                .execute();
            page_token = ret.getNextPageToken();
            for (User user : ret.getUsers())  {
                System.out.println("User: " + user.getName().getFullName());
                users.add(user);
            }
        } while (page_token != null && page_token.length() > 0);
        return users;
    }
    public Users getUsers(String email_address) throws IOException{
        Directory.Users.List dirUserList = directory.users().list()
            .setDomain(domain)
            .setQuery("email:" + email_address + "*");
        return dirUserList.execute();
    }
    public User createUser(String email_address, String fn, String sn, String ou) throws IOException {
        User user = new User();
        UserName name = new UserName();
        name.setFamilyName(sn);
        name.setGivenName(fn);
        user.setName(name);
        user.setOrgUnitPath(ou);
        user.setPassword(new BigInteger(128, new SecureRandom()).toString(32));
        user.setPrimaryEmail(email_address + "@" + domain);
        user = directory.users().insert(user).execute();
        return user;
    }

    public User setUserOrg(User user, String ou) throws IOException {
        user.setOrgUnitPath("/Delegated Accounts");
        Directory.Users.Update updateRequest = directory.users().update(user.getPrimaryEmail(), user);
        updateRequest.execute();
        return user;
    }

}
