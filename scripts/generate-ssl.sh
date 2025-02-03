echo "generating key 1"
openssl genrsa -des3 -out node1.key 3072

echo "generating csr 1"
openssl req -new -key node1.key -out node1.csr

echo "generating certificate 1 signed by key 1"
openssl x509 -req -days 365 -in node1.csr -signkey node1.key -out node11.crt

echo "generating key-store 1 containing certificate 1 signed by key 1"
openssl pkcs12 -export -in node11.crt -inkey node1.key -name "node1" -out node1.p12

echo "generating trust-store 2 containing certificate 1 signed by key 2"
keytool -import -file node11.crt -alias node1 -keystore node22.jks