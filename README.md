# nifi-s2s-test
Tests for [Apache NiFi](https://nifi.apache.org/) Site-to-Site.

```
# Run the test
mvn install exec:java -Dexec.mainClass=com.rumawaks.nifi.s2s.test.ScaleTest

# As a back-ground job
mvn install exec:java -Dexec.mainClass=com.rumawaks.nifi.s2s.test.ScaleTest > /mnt/nifi/test.log 2>&1 &
```
