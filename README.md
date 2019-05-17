# nifi-s2s-test
Tests for [Apache NiFi](https://nifi.apache.org/) Site-to-Site.

```
# Set heap size
export MAVEN_OPTS="-Xms8g -Xmx8g"

# Run the test
mvn install exec:java -Dexec.mainClass=com.rumawaks.nifi.s2s.test.ScaleTest

# As a back-ground job
mvn install exec:java -Dexec.mainClass=com.rumawaks.nifi.s2s.test.ScaleTest > /mnt/nifi/test.log 2>&1 &
```

```
# /etc/security/limits.conf
# or /etc/security/limits.d/20-nproc.conf

*                soft    nofile          10240
*                hard    nofile          10240
*                soft    nproc           10000
*                hard    nproc           10000
```
