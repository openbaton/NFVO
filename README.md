# OpenBaton

OpenBaton is an open source project providing a reference implementation of the NFVO and VNFM based on the ETSI [NFV MANO] specification. 

## Getting Started

`openbaton` is implemented in java using the [spring.io] framework. It consists of two main components: a NFVO and a generic VNFM. This document just describes its main architecture, and the role of the different components. It provides also a guide on how to prepare the infrastructure in order to setup an ETSI NFV compliant environment. 


## NFVO

## VNFM

How to write a Vnfm for OpenBaton:

* install vnfm-sdj-jms:

```sh
    $ cd openbaton-libs/vnfm-sdk-jms
    $ ./gradlew build install
```

* install vnfm-sdk gradle plugin:

    download and install the plugin here: [vnfm-plugin](https://gitlab.fokus.fraunhofer.de/openbaton/vnfm-sdk-gradle-plugin/blob/develop/README.md)

* create new gradle java project with build.gradle starting with:
```gradle
    buildscript {
        repositories{
            mavenCentral()
            maven {
                url uri('../repository-local')
            }
        }
        dependencies {
            classpath 'org.project.openbaton:vnfm-sdk-jms:0.1'
            classpath 'org.springframework.boot:spring-boot-gradle-plugin:1.2.5.RELEASE'
        }
    }

    apply plugin: 'spring-boot'
    apply plugin: 'vnfm-sdk'

    mainClassName = '<path_to_the_manager_main_class>'
    //...

```

* add a conf.properties file into the classpath (yes call it conf.properties):

```properties
    type=yourtype
    endpoint=yourtype-endpoint
```

* the manager main class needs to extend AbstractVnfmSpringJms and implements all the methods accordingly
* there must be a main method like this:
```java
    public static void main(String[] args) {
            SpringApplication.run(VnfmManagerMainClass.class);
    }
```

   where VnfmManagerMainClass is the name of your manager class

* please do not forget:
    * the manager main class is stateless and can (will) run each method in parallel potentially
    * extending AbstractVnfmSpringJMS you get out of the box tho methods:
        - *_boolean_ grantLifecycleOperation(virtualNetworkFunctionRecord)*:
            this method will start a particular communication with the NFVO returning true whenever there are enough resources for allocate the VNFR
        - *_boolean_ allocateResources(virtualNetworkFunctionRecord)*:
            this method will start a particular communication with the NFVO returning true when all the VNFComponents of the VNFR are allocated correctly on the defined VIM

## Version
0.5

## Installation

```sh
$ git clone [git-repo-url] openbaton
$ cd openbaton
$ ./openbaton.sh -i
```

### Debian package



## Plugins

`openbaton` provides some plugins in order to interact with the most common cloud platforms available:
* OpenStack
* Amazon EC2

### How to install a plugin


## Development

Want to contribute? Great!

## Todo's

Write Tests
Github saving overhaul
Code Commenting
Night Mode


## News and Website
Information about OpenBaton can be found on our website. Follow us on Twitter @[openbaton].

### License

[spring.io]:https://spring.io/
[NFV MANO]:http://docbox.etsi.org/ISG/NFV/Open/Published/gs_NFV-MAN001v010101p%20-%20Management%20and%20Orchestration.pdf
[openbaton]:http://twitter.com
