# Security

The Security in Openbaton is based on Outh2 provided by [Spring Security]. 

# How to get a token
>For making any request over the rest API you shoul find a token. The request for retrieving a token is:

```sh
curl -v -u openbatonOSClient:secret -X POST http://localhost:8080/oauth/token -H "Accept:application/json" -d "username=admin&password=admin&grant_type=password"
```

>the answer from the orchestrator is:

```sh
{"access_token":"d36b4941-5070-406e-a420-9ae7d16b5ddb","token_type":"bearer","refresh_token":"438edd63-190a-4afb-bb00-c0a5aed6a892","expires_in":43199,"scope":"read write"}
```

>Well now you can make a real request to the Rest API:

```sh
curl POST -d@vim-instance.json -v -H "Content-type: application/json" -H "Authorization: Bearer d36b4941-5070-406e-a420-9ae7d16b5ddb" localhost:8080/api/v1/datacenters
```



### Version
5.0.0

### Disable Security

If you won't waste your time making this two calls every new deploy, you can remove the Security commenting the row in the file **settings.gradle** in the main project **nfvo**:
>include 'security'

and in the module **main** in the file **build.gradle**:

>runtime project(':security')



[Spring Security]:http://projects.spring.io/spring-security-oauth/