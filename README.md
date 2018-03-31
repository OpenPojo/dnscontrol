## DNSControl [![Build Status](https://travis-ci.org/OpenPojo/dnscontrol.svg?branch=master)](https://travis-ci.org/OpenPojo/dnscontrol) [![Coverage Status](https://coveralls.io/repos/github/OpenPojo/dnscontrol/badge.svg?branch=master)](https://coveralls.io/github/OpenPojo/dnscontrol?branch=master) [![Maven Central](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/openpojo/dnscontrol/maven-metadata.xml.svg?style=flat&colorB=007ec6)](http://search.maven.org/#search|ga|1|g%3Acom.openpojo%20a%3Adnscontrol)
### Simplified control over DNS routing in JAVA
When you need more control on which DNS servers to use in Java for certain hosts or domains.

#### Quick startup
Create a DNS Control configuration file as such:
```properties
# Selection order is more specific wins
# For example, if you have a host, domain, and default override, they will be processed in that order.
# The left hand side is the destination to override, the right hand is the list of dns servers to use.
# Left hand entries should be unique, one entry per destination.
# Don't need to suffix your entries with "." dot, this is implied, meaning, default domain will not be appended.

# Setup default DNS servers, this is optional.
# If not defined, the current host DNS servers will be used if unset (i.e. resolv.conf).
.=10.0.0.1,10.0.0.2

# Override DNS lookups for specific hosts
www.openpojo.com=10.1.1.1

# Override DNS lookups for whole domains (starts with "." dot),
# This will override lookups for *.openpojo.com (i.e. some.host.openpojo.com)
.openpojo.com=10.0.2.2

# Suppress any queries that you'd like to never resolve
hiddenhost.openpojo.com=

# Finally if you want to use your current system's DNS servers without having to know the entries.
unchanged.resolution.openpojo.com=SYSTEM
```
startup your JVM passing NameService provider as well as DNS routing file
```sh
java -Dsun.net.spi.nameservice.provider.1=dns,dnscontrol -Ddnscontrol.conf.file=/path/where/dns_control_file
```
The default value for dnscontrol.conf.file is /dnscontrol.conf on root of the classpath.

##### Environment
Currently DNSControl runs on Java 1.7 & 1.8.

Builds on the functionality provided by [dnsjava](http://dnsjava.org/).
