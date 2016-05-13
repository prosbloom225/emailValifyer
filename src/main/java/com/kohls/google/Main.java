package com.kohls.google;

import java.io.*;
import java.util.List;
import java.util.Arrays;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.Users;
import com.google.api.services.admin.directory.model.UserName;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import com.kohls.google.Valifyer;

public class Main {
    // GLOBAL SEG
    protected static final Logger log = Logger.getLogger(Main.class.getName());
    protected DirectoryServiceHelper directoryHelper;
    // END GLOBAL

    // CNST SEG
    private static String APP_NAME = "emailValifyer";
    private static String DOMAIN = "gtest.kohls.com";
    private static String SVC_ACCT_EMAIL = "524816920325-tj75v0nolh6dr22fafodpbncma25dgkn@developer.gserviceaccount.com";
    private static String SVC_ACCT_USER = "michael.osiecki" + "@" + DOMAIN; // admin user
    private static java.io.File PRIVATE_KEY = new java.io.File("./private_key.p12");
    // END CNST

    public Main() {
        log.info("Begin");
        // Get list of users
        // Iterate users
        try {
            directoryHelper = new DirectoryServiceHelper(APP_NAME,
                    DOMAIN,
                    SVC_ACCT_USER,
                    SVC_ACCT_EMAIL,
                    PRIVATE_KEY);
            List<User> users = directoryHelper.getUsers();

            for (User user : users) {
                worker_user(user);
            }

        } catch (GeneralSecurityException e) {
            log.severe("Could not initialize admin directory service: " + e.toString());
        } catch (IOException e) {
            log.severe("Could not get list of users: " + e.toString());
        }

    }
    private void worker_user(User user) {
        log.info("worker_user: " + user.getPrimaryEmail());
        // Get list of drive docs
        try {
            DriveServiceHelper driveHelper = new DriveServiceHelper(APP_NAME,
                    DOMAIN,
                    user.getPrimaryEmail(),
                    SVC_ACCT_EMAIL,
                    PRIVATE_KEY);
            List<com.google.api.services.drive.model.File> files = driveHelper.getFiles(user.getPrimaryEmail());
            for (com.google.api.services.drive.model.File file : files) {
                List<Permission> permissions =  file.getPermissions();
                if (permissions != null) {
                    for (Permission permission : permissions) {
                        if (permission.getEmailAddress() != null && !permission.getEmailAddress().contains(DOMAIN)) {
                            log.info("Checking email: " + permission.getEmailAddress());
                            //  Remove share if invalid address
                            if (Valifyer.isAddressValid(permission.getEmailAddress())) {
                                log.info("Email: " + permission.getEmailAddress() + " is valid");
                            } else {
                                log.info("Email: " + permission.getEmailAddress() + " is invalid");
                                try {
                                    driveHelper.remPermission(file.getId(), permission.getId());
                                } catch (IOException e){
                                    log.warning("Could not remove permission: " + e.toString());
                                }
                            }
                        }
                    }
                }
            }
        } catch (GeneralSecurityException e) {
            log.severe("Could not initialize drive service: " + e.toString());
        } catch (IOException e) {
            log.severe("Could not read private key file: " + e.toString());
        }
        // Grab drive doc acl
        // Iterate acl
        // Valify email address
        // Action upon valify
    }


    private void test_valifyer() {
        String testData[] = {
            "michael.osiecki@kohls.com",
            "prosbloom@gmail.com",
            "michael@osieckim.com",
            "media@lifung.com",
            "prosbloom225@hotmail.com",
            "fail.me@nowhere.spam", // Invalid domain name
            "ajijcbw@asdfjiejsbe.net", // Invalid address
            "prosbloom225@yahoo.com" // Failure of this method
        };
        for ( int ctr = 0 ; ctr < testData.length ; ctr++ ) {
            try {
                log.info( testData[ ctr ] + " is valid? " +
                        Valifyer.isAddressValid( testData[ ctr ] ) );
            } catch (Exception e) {
                log.warning(e.toString());
            }

        }
    }
    public static void main(String[] args) {
        // stub
        Main instance = new Main();
    }
}
