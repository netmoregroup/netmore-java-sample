# <img src="https://avatars.githubusercontent.com/u/93947921?s=200&" width="32"/> Netmore Cloud Control sample application.

This sample application is written in java, for payload information 
see the javascript sample

## Setup.
Unpack the certs you have received into the certs directory, when done it should look
like below where CommonName is extracted from the certificate (this normally the same as customerId)

    ls ./certs/<CommonName>/
    ca.crt
    client.crt
    client.key

Create a p12 file to import into the java keystore.

    openssl pkcs12 -export -out client.p12 -inkey client.key -in client.crt

## Install dependencies
Install the mqtt node libraries using maven.

    mvn install

## Run
In order to run the application you need the common name from the certificate
this can be retrieved in various ways but the simplest is

    openssl x509 -in ./client.crt -noout -text

Compile the code:

    mvn compile

Once you found the CN short for common name you can add that as the last
argument to the command below.

    mvn exec:java -D exec.args="<commonName>"
