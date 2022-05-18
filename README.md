# Purpose 
The purpose of this repo is to show how one can avoid passing in plaintext credentials
within `server.properties`, as well as during initial client authentication check. 

Kafka allows us to specify server callback handlers, which is the mechanism that brokers will use to  determine whether credentials given by the client are authentic or not. In this example, we pass in multiple encrypted flat-files into the brokers `sasl.jaas.config` and the path to the callback handler. The implementation shows that on each intial client connection established, the handler will be invoked by the broker, and the callback queries the given files to verify an entry exists. If it does, we allow the client to establish the connection.

Kafka also allows us to specify client callback handlers to load passwords dynamically at runtime when a connection is established instead of loading statically from the JAAS configuration during startup. Clients don't have to pass in plaintext passwords, and can instead specify the file which holds client credentials (`clientCB.properties` passes in the path to the file holding its credentials)




### creating encrypted password files
- `htpasswd -c <filename>.props <username>`
- enter password
- re-enter password

### manually verify 
- `htpasswd -vb <filename>.props <username> <password>`
- should return `Password for user <username> is correct`
- this exact call will be made my server callback handler to determine authentic credentials

### How to run
After creating the flat-files used to store usernames and password for basic authentication, ensure the location is specified with respect to the docker-compose file. 

`PasswordVerifier` will store the file locations within `passwdFiles`. When a client attempts to authenticate with the broker, broker delegates authentication validation to `PasswordVerifier`, which will iterate over each file (passed within env variable - `sasl.jaas.config`) to see if an entry with passed client credentials exists.

- run `docker compose up -d`
- run `kafka-topics --bootstrap-server localhost:9092 --create --topic test --partitions 1 --replication-factor 1 --command-config client.properties`
- run producer/consumer with either client.properties or client-2.properties
  - Ensure the user/pass matches 




### using client callback handler
- `client-cb-passwds.props` is the file which client callback handler will query to get credentials to pass to the broker, as opposed to passing in the credentials in-line when connecting to the broker
- run `export CLASSPATH=<path/to/security-cbs-1.0-SNAPSHOT.jar>`
- `kafka-topics --bootstrap-server localhost:9092 --create --topic test --partitions 3 --replication-factor 1 --command-config clientCB.properties` 
- after client callback retrieves credentials, the server callback handler will verify that an entry user `client` exists in encrypted flat-file `security/client.props`


NOTE: username & password specified in resources/client-cb-passwds.props must match with encrypted credentials in ${root}/security/client.props




#### Credentials used
1. nermin.props
   - username: nermin
   - password: secret
   - client file to pass in : client.properties
2. nejra.props
   - username: nejra
   - password: secret
   - client file to pass in : client-2.properties
3. client.props
   - username: client
   - password: secret
   - client file t0 pass in : clientCB.properties
   - clientCB.properties does not contain plaintext credentials. Instead, passes in the file to be used (for now, doesn't use, since the implementation is querying static file passed - but value in code matches what is passed in clientCB.properties filepath)

NOTE: nermin.props,nejra.props,client.props are referenced in server.properties





