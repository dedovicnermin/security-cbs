package io.nermindedovic.securitycbs;

import org.apache.kafka.common.security.plain.internals.PlainServerCallbackHandler;

import javax.security.auth.login.AppConfigurationEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * THIS IS A CUSTOM IMPLEMENTATION OF SERVER CALLBACK HANDLER USED BY THE BROKER
 *
 * When using SASL_PLAIN in production, a custom server callback handler can be
 * used to integrate brokers with a secure third-party password server.
 * Custom callback handlers can also be used to support password rotation.
 * On the server side, a server callback handler should support both old and
 * new passwords for an overlapping period until all clients switch to the new password.
 * The following example shows a callback handler that verifies encrypted passwords from
 * files generated using the Apache tool htpasswd
 */
public class PasswordVerifier extends PlainServerCallbackHandler {

    private final List<String> passwdFiles = new ArrayList<>();

    @Override
    public void configure(Map<String, ?> configs, String mechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        System.out.println("nermin!: "+jaasConfigEntries.get(0).getOptions());
        final Map<String, ?> loginOptions = jaasConfigEntries.get(0).getOptions();
        final String files = (String) loginOptions.get("password.files");
        Collections.addAll(passwdFiles, files.split(","));
    }


    @Override
    protected boolean authenticate(String username, char[] password) {
        return passwdFiles.stream()
                .anyMatch(file -> authenticate(file, username, password));
    }

    private boolean authenticate(String file, String username, char[] password) {
        try {
            final String cmd = String.format("htpasswd -vb %s %s %s", file, username, new String(password));
            return Runtime.getRuntime().exec(cmd).waitFor() == 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
          return false;
        }
    }
}
