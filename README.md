# file-processing-java-microservice
A Microservice that processes the uploaded Text file and creates an outcome JSON file with selected contents out of the original Text file.

## Installation (Locally):
### Pre-requisites:
This project/ Java Spring-boot microservice requires a MySQL server setup on you system prior to running the service.
You can download the same from here: https://www.mysql.com/downloads/ based on your OS.

### Steps:
* Clone the Repo from: `https://github.com/jasmira/file-processing-java-microservice`.
* Import the code in any IDE (IntelliJ IDE preferred).
* Download all necessary Maven dependencies and libraries (With IDE, this is handled automatically).
* Open file `FileProcessorApplication.java` and right click on it and select `Run ...` option. 
* This should start your java microservice locally in your IDE.


# REST APIs:
This microservice exposes below 3 APIs:

## Process File
### Request
`POST /api/process-file`

This API takes 3 parameters:
* `file`: uploaded `EntryFile.txt`
* `ipAddress`: any IP Address
* `skipValidation`: a boolean value of `true` or `false`

### Responses:
* If `skipValidation = false` or `skipValidation = true` and a valid `ipAddress` is used along with a non-empty file uploaded.
```agsl
[
    {
        "Name": "John Smith",
        "Transport": "Rides A Bike",
        "Top Speed": 12.1
    },
    {
        "Name": "Mike Smith",
        "Transport": "Drives an SUV",
        "Top Speed": 95.5
    },
    {
        "Name": "Jenny Walters",
        "Transport": "Rides A Scooter",
        "Top Speed": 15.3
    }
] 
```
Additionally, you will see a file `OutcomeFile.json` created at location `src/main/resources/` in your code, with the above JSON response in it.

* If `skipValidation = false` and a `ipAddress` of a Blocked Country is used with a non-empty file uploaded.
```agsl
{
    "valid": false,
    "blockedCountry": true,
    "blockedISP": false,
    "country": "United States",
    "countryCode": "US",
    "isp": "Google LLC",
    "reason": "Access from the country: United States is not allowed.",
    "httpStatusCode": 403
}
```
**NOTE: Blocked countries are "United States", "China" and "Spain"**

* If `skipValidation = false` and a `ipAddress` of a Blocked ISP is used with a non-empty file uploaded.
```agsl
{
    "valid": false,
    "blockedCountry": false,
    "blockedISP": true,
    "country": "India",
    "countryCode": "IN",
    "isp": "Amazon Technologies Inc.",
    "reason": "Access from the ISP: Amazon Technologies Inc. is not allowed.",
    "httpStatusCode": 403
}
```
**NOTE: Blocked ISPs are "AWS", "Google Cloud" and "Microsoft Azure"**

## Validate IP Address
### Request
`GET /api/validate-ip?ipAddress=<YOUR_IP_ADDRESS>`

This API takes 1 parameter:
* `ipAddress`: any IP Address.

### Response
```agsl
{
    "valid": true,
    "blockedCountry": false,
    "blockedISP": false,
    "country": "Indonesia",
    "countryCode": "ID",
    "isp": "Politeknik Perkapalan Negeri Surabaya",
    "reason": "IP address is whitelisted.",
    "httpStatusCode": 200
}
```

## Validate IP Address Details
### Request
`GET /api/validate-ip/details?ipAddress=<YOUR_IP_ADDRESS>`

This API takes 1 parameter:
* `ipAddress`: any IP Address.

### Response
```agsl
IP address is whitelisted.
```


# Testing (Locally)
* You can test this microservice locally using POSTMAN or INSOMNIA client.
* I have added the POSTMAN collection JSON file in this project under the root folder with name `File Processing Service.postman_collection.json`.
* You can simply import this file in you POSTMAN client, and you will have access to all the APIs with different scenarios.
* Additionally, there are unit and integration test cases written for this microservice. 
* To run the test suite, go to location `src/test/java` in your IDE explorer, right-click on the `java` folder and select option `Run All Tests`. This should automatically run all the tests in the test suite.

**NOTE: I have used Wiremock to mock the external API: http://ip-api.com/json/{query} in the `IPValidatorServiceTest.java` file.**

**NOTE: To test the POST API in POSTMAN client, you have to upload the "EntryFile.txt" as one of the parameter. Refer below steps on how we actually do it:**
1. In the request body section, make sure you have selected form-data.
2. In the key-value pairs, for Key: file, Value: Select File from the dropdown, then choose the "EntryFile.txt" from your local file system.


#### Some Example IP addresses you can use for testing:
* IP addresses from the USA: 
  * 8.8.8.8
  * 50.207.42.33
* IP addresses from Spain: 
  * 81.35.0.0
  * 88.3.0.0
* IP addresses from China: 
  * 1.80.0.0
  * 14.0.0.0

* For ISPs:
  * England and Azure: 51.104.9.100
  * India and AWS: 52.66.193.64
  * South Africa and GCP: 34.152.86.0

* IP addresses from India:
  * 103.24.49.0
  * 103.24.50.0
  * 103.24.51.0
  * 103.24.52.0
  * 103.24.53.0

* IP addresses from Switzerland:
  * 185.156.73.0
  * 185.199.94.0
  * 185.206.224.0
  * 185.230.144.0
  * 193.34.217.0

* Invalid IP address: 
  * 127.0.0.1


Happy Testing!!! :) 