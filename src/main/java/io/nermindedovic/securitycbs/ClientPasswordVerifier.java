package io.nermindedovic.securitycbs;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * On the client side, a client callback handler can be used to load passwords dynamically at runtime
 * when a connection is established instead of loading statically from the JAAS configuration during
 * startup. Passwords may be loaded from encrypted files or using an external secure server to improve
 * security. The following example loads passwords dynamically from a file using configuration classes
 * in Kafka
 */
public class ClientPasswordVerifier implements AuthenticateCallbackHandler {

    /**
     *
     * @param map all properties that client is configured with. Referenced in notes/ClientPasswordVerifier-map-output.txt
     * @param s == what has been passed for prop sasl.mechanism (in our case, PLAIN)
     * @param list contains one entry - file=path/to/file (client-cb-passwds.props)
     */
    @Override
    public void configure(Map<String, ?> map, String s, List<AppConfigurationEntry> list) {
        System.out.println("Map: " + map.toString());
        System.out.println("s: " + s);
        System.out.println("List<AppConfigurationEntry> : " );
        list.forEach(System.out::println);
        final AppConfigurationEntry appConfigurationEntry = list.get(0);
        final String loginModuleName = appConfigurationEntry.getLoginModuleName();
        final Map<String, ?> options = appConfigurationEntry.getOptions();
        System.out.println("loginModuleName: " + loginModuleName);
        System.out.println("options:" + options.toString());
    }

    @Override
    public void close() {}

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        final Properties props = new Properties();
        try (final InputStream resourceAsStream = ClientPasswordVerifier.class.getClassLoader().getResourceAsStream("client-cb-passwds.props")) {
            props.load(resourceAsStream);
        }
        final PasswordConfig config = new PasswordConfig(props);
        final String user = config.getString("username");
        final String password = config.getPassword("password").value();
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                ((NameCallback) callback).setName(user);
            } else if (callback instanceof PasswordCallback) {
                ((PasswordCallback) callback).setPassword(password.toCharArray());
            }
        }
    }

    private static class PasswordConfig extends AbstractConfig {
        static final ConfigDef CONFIG = new ConfigDef()
                .define("username", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "User name")
                .define("password", ConfigDef.Type.PASSWORD, ConfigDef.Importance.HIGH, "User password");
        PasswordConfig(Properties properties) {
            super(CONFIG, properties, false);
        }
    }
}
