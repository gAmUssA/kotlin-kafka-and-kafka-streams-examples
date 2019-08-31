## TLS Security

Some pre reading to discuss difference between truststore (used to store public certificates) and keystore (used to store private certificates) [here](https://www.tutorialspoint.com/listtutorial/Difference-between-keystore-and-truststore-in-Java-SSL/4237)

#### Generate Keystore

`keytool -keystore kafka.server.keystore.jks -alias localhost -validity 365 -genkey`

#### CA Authority and Creating Trust Store

Need a CA certificate which is a `.pem` file. Elastic provide good documentation for setting your own [Certificate Authority](https://www.elastic.co/guide/en/shield/current/certificate-authority.html).
You can use self signed certificates such as exampled here by [Kafka](https://docs.confluent.io/2.0.0/kafka/ssl.html).

`openssl req -new -x509 -keyout ca-key -out ca-cert -days 365` creates the `ca-cert` and `ca-key`.

Lets now add the created certs to the trust store of the client so clients trust certificates signed by this CA. To quote Kafka 
"Importing a certificate into one’s truststore also means trusting all certificates that are signed by that certificate."
"You can sign all certificates in the cluster with a single CA, and have all machines share the same truststore that trusts the CA."

Server (inter broker communication) trust store import CA certificate so it trusts any certificate signed by CA.

`keytool -keystore kafka.server.truststore.jks -alias CARoot -import -file ca-cert`

Client (apps connecting too Kafka) trust store import CA certificate so it trusts any certificate signed by CA.

`keytool -keystore kafka.client.truststore.jks -alias CARoot -import -file ca-cert`

#### Signing the certificate

Export certificate from the Keystore:

`keytool -keystore kafka.server.keystore.jks -alias localhost -certreq -file cert-file`

Sign it with the generated CA created above.

`openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days 365 -CAcreateserial -passin pass:password`

Now the private key is signed from the keystore we need to import it again and the CA certificate.

`keytool -keystore kafka.server.keystore.jks -alias CARoot -import -file ca-cert
 keytool -keystore kafka.server.keystore.jks -alias localhost -import -file cert-signed`
 
 As noted by Kafka blog post
 
 The definitions of the parameters are the following:
 
 * keystore: the location of the keystore
 * ca-cert: the certificate of the CA
 * ca-key: the private key of the CA
 * ca-password: the passphrase of the CA
 * cert-file: the exported, unsigned certificate of the server
 * cert-signed: the signed certificate of the server
 
 
 ### Running the secure cluster
 
 You will need to generate the files for the broker communication and then can specify there location with the provided 
 environment variable at run time. 
 
 https://docs.confluent.io/5.0.0/installation/docker/docs/installation/clustered-deployment-ssl.html
 


