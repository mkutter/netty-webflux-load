# Spring webflux test application

When running a simple spring-boot app using webflux on netty, I experienced
strange spurious "wrong" responses.

"Wrong" means that sometimes (i.e. one in 10.0000 requests) under slightly
heavier load, the application returned a wrong response. 

You read correctly: Not an error, a wrong response (like an echo service 
asked to echo "dog", and once in a while responding "cat".)

This struck me. I would have expected any exception - but not a "wrong" 
response.

After some digging, I found out that these spurious wrong responses seem 
to be connected with Spring Security: When I switched on basic auth (or 
used an auth filter using HTTP headers), the errors happened. When I did 
not, they did not.

I built this simple test application to reproduce the error.

# Application building blocks

## Spring Boot Application

The application features a simple RestController, which just echoes back 
the request.

The `Application` class contains the Spring Boot application, and the 
security configuration.

UserDetails are provided by a fake `UserDetailsService` (in `Application`),
which always returns fixed UserDetails.

The request object is deserialized from json into `Request` and back again 
as response.

# JMeter test

The test is implemented as jmeter test. It uses `test/jmeter/SimpleRequest.jmx` 
as test file.

The test starts up the spring boot application, 
uses 100 parallel clients to hammer on it for a minute, 
and shuts down the application again. 

It always issues the same request:

`
{"subject":{"username":"USERNAME"},"action":"HTTP:ACTION","resource":"https://resource.url"}
`

The jmeter test script checks whether the responses equals the request.

A "wrong" response typically looks like this (wrong `action` field).
I've seen all fields' values garbled (one at a time):

`
{"subject":{"username":"USERNAME"},"action":"https://res","resource":"https://resource.url"}
`

Test results are written to `target/jmeter`.

Responses that do not match the expected result are written to 
`target/jmeter/results/failedRequest.*`

> Caveat: When running tests manually with jmeter, failed responses will
> be written to ../results/failedRequest.* - relative to jmeter's working
> directory.

The test aborts when a wrong response is detected.

# How to run the test

Simply execute `mvn clean verify`

## Expected result

The build succeeds

## Result reproducing the error described above

The build fails with a message like 
`Failing build because failed requests have been detected.  JMeter logs are available at: 'target/jmeter/logs'`

# Behavior on my machine

On my box, `mvn clean verify` consistently fails with the error described 
above, usually after a few seconds.

My box:

   * Ubuntu 17.10
   * Java:

   ```
   > java -version
   java version "1.8.0_181"
   Java(TM) SE Runtime Environment (build 1.8.0_181-b13)
   Java HotSpot(TM) 64-Bit Server VM (build 25.181-b13, mixed mode)
   ```
   
   I also tried OpenJDK and IBM java, with same results.

   * Hardware: i7 6700T, 16GB RAM

# Other things I tried

   * I switched to undertow. The error described above did not happen
   (but I got hanging requests).
   * I tried whether the request already reaches the application garbled
   by setting a conditional breakpoint. I found out it does.
   * I looked whether jmeter sends a garbled request by sniffing on the 
   connection with wireshark. I found out it does not.

# Suspected causes

I suspect there's an threading error somewhere in the deserialization
of requests (I've seen requests already reach the application garbeled by 
using a conditional breakpoint). To me it looks like the deserialization 
logic reuses a buffer, and it's read before it is initialized (or the 
desired value is overwritten before it is read).

The error may be caused by delay imposed by authentication processing.
 
