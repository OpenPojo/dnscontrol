## DNSControl [![Build Status](https://travis-ci.org/OpenPojo/dnscontrol.svg?branch=master)](https://travis-ci.org/OpenPojo/dnscontrol) [![Coverage Status](https://coveralls.io/repos/github/OpenPojo/dnscontrol/badge.svg?branch=master)](https://coveralls.io/github/OpenPojo/dnscontrol?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.openpojo/dnscontrol/badge.svg?style=flat)](http://search.maven.org/#search|ga|1|g%3Acom.openpojo)
### Simplified control over DNS routing in JAVA
When you need more control on which DNS servers to use in Java for certain hosts or domains.

#### Quick startup
Create a DNS Control configuration file as such:
```properties
# Setup default DNS servers
.=10.0.0.1,10.0.0.2
# Override DNS lookups for specific hosts
www.openpojo.com=10.1.1.1
# Override DNS lookups for specific whole domains
.openpojo.com=10.0.2.2
# Suppress any queries that you'd like to never resolve
hiddenhost.openpojo.com=
```

startup your JVM passing NameService provider as well as DNS routing file
```sh
java -Dsun.net.spi.nameservice.provider.1=dns,dnscontrol -Ddnscontrol.conf.file=/path/where/dns_control_file
```

Builds on the functionality provided by [dnsjava](http://dnsjava.org/).

_Coming soon!_
