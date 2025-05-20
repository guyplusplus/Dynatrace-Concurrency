# Dynatrace Concurrency

This utility reproduces the capability of [`concurrency`](https://docs.splunk.com/Documentation/SplunkCloud/latest/SearchReference/Concurrency) of Splunk.

> Concurrency measures the number of events which have spans that overlap with the start of each event. Alternatively, this measurement represents the total number of events in progress at the time that each particular event started, including the event itself. This command does not measure the total number of events that a particular event overlapped with during its total span.

For each event (log entry, etc.), a new 'concurrency' key is added. For example:

```text
messages.processstarttime,messages.processingtime,concurrency
2025-05-10T09:12:00.243Z,853,1
2025-05-10T09:12:00.735Z,40,2
2025-05-10T09:12:04.426Z,784,1
2025-05-10T09:12:04.686Z,340,2
2025-05-10T09:13:44.210,30,1
```

Additionally this tool creates a summarized table with time slots, showing the number of events and the maximum concurrency during this interval. For example:

```text
Timeslot date-time,Event count,Max concurrency
2025-05-10T09:12:00Z,4,2
2025-05-10T09:13:00Z,1,1
```

The reports concludes with the following information that is important to read to possibly tune the request. It shows in particular the maximum concu
across all events.

```text
Engine processed events: 5
Engine max Concurrency: 2
Grail stats: scannedRecords:72670, scannedBytes:3768611
Grail notification: WARNING: Your result has been limited to 5.
Analysis complete.
```

This utility relies on Grail API, information can be found [here](https://developer.dynatrace.com/plan/platform-services/grail-service/). Swagger UI can be found at `https://**yourSite**.apps.dynatrace.com/platform/swagger-ui/index.html?urls.primaryName=Grail%20-%20DQL%20Query#/Query%20Execution/query%3Aexecute` .

## Algorithm

The utility keeps a sorted list of events end-time.

Events arrive in chronological start-time ascending order. When an event arrives, end-time entries strickly before this event start-time are removed from the sorted list. This event concurrency is equal to the number of elements in the list.

This event end-time is then added to the sorted list.

## Utility invocation

Simply call the java JAR file, by indicating optionally `-events` to output raw events with their calculated concurrency (the first table above), and optionally `-timeslot 60` to output summarized output, in this sample example with an interval of 60 seconds.

`java -jar dynatraceConcurrency-n.m-jar-with-dependencies.jar -events -timeslot 60`

In the case the Grail DQL output does not refer to the standard columns `messages.processstarttime` and `messages.processingtime`, it is possible to overwrite these with the command line arguments `-field.startTime fieldName` and/or `-field.duration fieldName`.

`java -jar dynatraceConcurrency-n.m.jar -?` outputs the different arguments.

## Configuration files

### dql.json

The `dql.json` contains the main DQL statement and the start and end times. It contains also additional tuning parameters, mostly timeouts (`fetchTimeoutSeconds` and `requestTimeoutMilliseconds`) and maximum query and return size (`maxResultRecords` and `maxResultBytes`). These values can be adjusted based on a possible warning message included in the response.

**Important: events must be sorted by messages.processstarttime ascending order.**. The utility detects and stops if it is not the case. Events must carry start time and duration. It is recommended to limit to these fields to reduce egress traffic. For example:

`fetch logs | filter isNotNull(messages.processstarttime) | fields messages.processstarttime, messages.processingtime | sort messages.processstarttime asc | limit 10000`

DQL syntax guide can be found [here](https://docs.dynatrace.com/docs/discover-dynatrace/references/dynatrace-query-language).

Query parameters can be found [here](https://developer.dynatrace.com/develop/sdks/client-query/#executerequest).

### dynatraceAccount.secrets

The `dynatraceAccount.secrets` file contains the API key, your SaaS instance URL, and optionally some web proxy information to connect to.

An API token must be created [here](https://myaccount.dynatrace.com/platformTokens) with only logs or events READ access permission. Your own login is required. Alternatively the API token can be created by a site administrator.

## Save and load Grail responses

It is possible to save a Grail response with the command line argument `-saveDQLResultsFileName myFile.out`, and load it later without any new Grail query with the argument `-loadDQLResultsFileName myFile.out`. 
