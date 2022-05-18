### creating encrypted password files
- `htpasswd -c <filename>.props <username>`
- enter password

### manually verify 
- `htpassword -vb <filename>.props <username> <password>`
- should return `Password for user <username> is correct`

### How to run
After creating the flat-files used to store usernames and password for basic authentication, ensure the location is specified with respect to the docker-compose file. 

`PasswordVerifier` will store the file locations within `passwdFiles`. When a client attempts to authenticate with the broker, broker delegates authentication validation to `PasswordVerifier`, which will iterate over each file (passed within env variable - `sasl.jaas.config`) to see if an entry with passed client credentials exists.

- run `docker compose up -d`
- run `kafka-topics --bootstrap-server localhost:9092 --create --topic test --partitions 1 --replication-factor 1 --command-config client.properties`
- run producer/consumer with either client.properties or client-2.properties
  - Ensure the user/pass matches 


