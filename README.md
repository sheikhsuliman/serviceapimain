# SwissCon Service API [![CircleCI](https://circleci.com/gh/siryus-ag/swisscon-service-api.svg?style=svg&circle-token=03ea209d9610fd1988aabc1563b18ceef25de6fe)](https://circleci.com/gh/siryus-ag/swisscon-service-api)

A fully functional back-end prototype using [Spring Boot](https://spring.io/projects/spring-boot) 

## Build and Run

Prerequisites: JDK 11 or higher. See also the alternative [IntelliJ Setup](https://github.com/siryus-ag/swisscon-service-api/wiki/IntelliJ-Setup)

Checkout:

	git clone https://github.com/siryus-ag/swisscon-service-api.git
	
Build with tests:

	./mvnw clean install
	
Run (all environment variables set):

	./mvnw clean spring-boot:run
	
Run (locally):
Mail and AWS Environment variables set to defaults. But they won't connect, but you can still run the app.
To have AWS/Mail Functionality you need to define
these environment variables in your run configuration or your a ~/.swisscon file:

* export AWS_SECRET_ACCESS_KEY=xxx
* export AWS_ACCESS_KEY_ID=xxx
* export MAIL_HOST=smtp.xx.com
* export MAIL_USERNAME=xxx@xxx.com
* export MAIL_PASSWORD=xxx


Run with script
    
    ./scripts/run.sh
	
**NOTE** Once app is running, API Documentation could be viewed at: http://localhost:8080/swagger-ui.html

### Run using `scripts/run.sh`

On *nix systems, better way to run the app is via

    ./scripts/run.sh
    
This will run the app in exactly the same way as it run in Docker container. And unfortunately we have experienced
some problems with the app when it was run NOT exactly as it does in Docker container.   	 
	
### Connecting to the App DB  (Postgres)

When run locally, the app uses embedded Postgres as a DB. You can connect to the server using any Postgresql client using 
this URL

    jdbc:postgresql://localhost:65432/postgres

**NOTE** The embedded Postgres server will stop once last client is disconnected from it. Which means that if you use Postgres 
client to inspect the DB, the server will **NOT** stop when you stop the App... if you do want DB to restart, you need to 
disconnect you Postgres client from it.

### Build profile `postgresql` locally

If you want to build profile `postgresql` on your local box, you have to start postgres (in other terminal) using following
command

```bash
docker run -e POSTGRES_DB=swisscon -e POSTGRES_HOST_AUTH_METHOD=trust  -p 5432:5432 postgres:10.6-alpine
```

Once you see 

```
PostgreSQL init process complete; ready for start up.
```

you can run maven

```bash
./mvnw -nsu -P postgresql clean install
```

## Environment Variables

Look into application.properties and appliation-{SPRING_PROFILES_ACTIVE}.properties for environment variables

We use H2 Profile just for local development and for one build pipeline. But in all environments which are deployed > we will use a PostgreSQL Database.

| Variable | Description |
| --- | ---  |
| ENVIRONMENT | Which environment is used > could be dev, test, stage or prod. This has influence which flyway scripts are executed for the data and which S3 Bucket is used for file storage | 
| APPLICATION_URL | The UI URL of the Auth Module (used for mail verification links) | 
| LEMON_CONFIRMATION_LINK | path, following the application url to confirm the mail | 
| LEMON_INVITATION_LINK | path, following the application url to invite other users | 
| LEMON_RESET_LINK | path, following the application url to reset the password | 
| MAIL_HOST | The SMPT Mail Host Address |
| MAIL_USERNAME | The mail address you use to send mails |
| MAIL_PASSWORD | Email Password |
| AWS_NAMECARD_BUCKET | The S3 bucket name for the file storage |
| AWS_ACCESS_KEY_ID | Access Key for the AWS technical user |
| AWS_SECRET_ACCESS_KEY | Secret Access Key for the AWS technical user |
| JWT_SECRET | JWT Authorization Token. An aes-128-cbc key generated at https://asecuritysite.com/encryption/keygen (take the "key" field) |
| JAVA_HOME | Folder of the Java installation |
| M2 | Folder with the Maven binary file |
| M2_Home | Folder of the Maven installation |
| SPRING_PROFILES_ACTIVE | Current Spring Profile > could be 'h2' or 'postgresql' |
| DB_NAME | Name of the database |
| DB_HOST | Host of the database |
| DB_PORT | Port of the database (5432 default for PostgreSQL) |
| DB_PORT | Port of the database |
| DB_USERNAME | Username for the database |
| DB_PASSWORD | Password for the database |
| SERVER_PORT | The Port of the Spring-Application (This *has to be always 8080* because we expose this port in our Docker container |
| FLYWAY_CLEAN_ON_VALIDATION_ERROR | Cleans the DB on Script Checksum Errors > this should be false in prod environment |
| DEFAULT_SIGNUP_TOKEN | For the verify-signup-token / signup endpoints > We can use this token to signup without invitation |

## Flyway Scripts

Flayway Scripts are executed on every *boot* of the spring application.
They are executed according to their script number ascending.

Flyway Scripts are located in 

* *src/main/resources/db/migrations*: SQL Flayway Scripts
* *src/java/db/migrations*: Java Flyway Scripts

### Flyway Folders explained

* **common**: All Build Scripts which create or modify tables/columns
* **data/common**: Data Scripts which are needed in all environments
* **data/{ENVIRONMENT}**: Data Scripts which are specific for one environment (dev, test, stage, prod). It depends on the environment Variable ENVIRONMENT
* **postgresql**: PostgreSQL specific Scripts


## Testing

To run all tests

    ./mvnw clean package

To run all unit tests, but skip all integration tests

    ./mvnw -P noIT clean package
    
Single tests can be executed using 

    ./mvnw -Dtest=ClassNameWhereTestsReside#TestMethodName test

Where `ClassNameWhereTestsReside` is the class where the test `TestMethodName` lives.

In case you want to execute all tests from a single class use

    ./mvnw -Dtest=ClassNameWhereTestsReside test

In case you'd like to quickly compile the backend without running any test do

    ./mvnw clean install -DskipTests --quiet


### Local Testing of API end points


To test API end points using curl, it is necessary to populate cookie jar with secure cookie. This could be done using following command:

```bash
curl -c cookie.txt -X POST -F 'username=test.albin-borer-ag.ch@siryus.com' -F 'password=cocoTest6' https://localhost:8080/api/rest/auth/login -k -v
```

Once cookie jar (`cookie.txt`) been populated, it should be used for all following API calls. For example:

```bash
curl -k -b cookie.txt https://localhost:8080/api/rest/companies/1/team
```







	



	



# serviceapimain
