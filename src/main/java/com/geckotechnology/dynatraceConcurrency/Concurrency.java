package com.geckotechnology.dynatraceConcurrency;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

public class Concurrency {
	
	private static final String DYNATRACE_ACCOUNT_SECRETS_FILENAME = "dynatraceAccount.secrets";
	private static final String DQL_JSON_FILENAME = "dql.json";
	
	private boolean loadDQLResultsFromFileFlag = false;
	private String loadDQLResultsFileName = null;
	private boolean saveDQLResultsToFileFlag = false;
	private String saveDQLResultsFileName = null;
	private boolean eventsOutputFlag = false;
	private boolean timeslotStatisticsOutputFlag = false;
	private int timeslotDurationInSeconds = -1;
	private String messagesProcessStartTimeField = "messages.processstarttime";
	private String messagesProcessingTimeField = "messages.processingtime";
	
	private JsonObject dqlResults = null;
	private ArrayList<TimeslotStatistics> statisticsArrayList = null;
	
	private void printHelp() {
		System.out.println("java concurrent.jar [-h/-help/-?] [-events] [-timeslot timeslotDurationInSeconds] [-loadDQLResultsFileName filename] [-saveDQLResultsFileName filename] [-field.startTime fieldName] [-field.duration fieldName] [-proxy host:port] [-proxy-user usernam:pwd]");
	}
	
	private void initData(String[] args) {
		int n = 0;
		int argslength = args.length;
		while(n < argslength) {
			if("-help".equals(args[n]) || "-h".equals(args[n]) || "-?".equals(args[n])) {
				printHelp();
				n++;
				continue;
			}
			if("-loadDQLResultsFileName".equals(args[n])) {
				loadDQLResultsFromFileFlag = true;
				loadDQLResultsFileName = args[++n];
				n++;
				continue;
			}
			if("-saveDQLResultsFileName".equals(args[n])) {
				saveDQLResultsToFileFlag = true;
				saveDQLResultsFileName = args[++n];
				n++;
				continue;
			}
			if("-events".equals(args[n])) {
				eventsOutputFlag = true;
				n++;
				continue;
			}
			if("-timeslot".equals(args[n])) {
				timeslotStatisticsOutputFlag = true;
				timeslotDurationInSeconds = Integer.parseInt(args[++n]);
				n++;
				continue;
			}
			if("-field.startTime".equals(args[n])) {
				messagesProcessStartTimeField = args[++n];
				n++;
				continue;
			}
			if("-field.duration".equals(args[n])) {
				messagesProcessingTimeField = args[++n];
				n++;
				continue;
			}
			n++;
		}
	}
	
	private void validateInitData() {
		if(timeslotStatisticsOutputFlag && timeslotDurationInSeconds <= 0)
			throw new RuntimeException("timeslot duration must be stricly positive");
		if(!eventsOutputFlag && !timeslotStatisticsOutputFlag) //nothing to do, may be show help only
			System.exit(0);
	}
	
	private void loadDQLResultsFromFile() {
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(loadDQLResultsFileName), 10240);
			JsonReader rdr = Json.createReader(bis);
			dqlResults = rdr.readObject();
			bis.close();
			System.out.println("DQLResults loaded sucessfully from file " + loadDQLResultsFileName);
		} catch (Exception e) {
			System.err.println("Problem loading the DQL outout file " + loadDQLResultsFileName);
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void loadDQLResultsFromAPI() {
		try {
			Properties secretsProps = new Properties();
			FileInputStream fis = new FileInputStream(new File(DYNATRACE_ACCOUNT_SECRETS_FILENAME));
			secretsProps.load(fis);
			fis.close();
			System.out.println("Retrieving data from API at " + secretsProps.getProperty("dynatraceSaaSURL"));
			String proxy = secretsProps.getProperty("proxy");
			String proxyUser = secretsProps.getProperty("proxyUser");
			HttpClient.Builder clientBuidler = HttpClient.newBuilder()
					.version(Version.HTTP_2)
					.followRedirects(Redirect.NEVER)
					.connectTimeout(Duration.ofSeconds(30));
			if(proxy != null) {
				clientBuidler = clientBuidler.proxy(ProxySelector.of(
						InetSocketAddress.createUnresolved(proxy.substring(0, proxy.indexOf(':')),
								Integer.parseInt(proxy.substring(proxy.indexOf(':') + 1)))));
				if(proxyUser != null) {
					clientBuidler = clientBuidler.authenticator(new Authenticator() {
	                    @Override
	                    protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(proxyUser.substring(0, proxy.indexOf(':')),
                            		proxyUser.substring(proxy.indexOf(':') + 1).toCharArray());
	                    }
	                });
				}
			}
			HttpClient client = clientBuidler.build();
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create(secretsProps.getProperty("dynatraceSaaSURL") + "/platform/storage/query/v1/query:execute?enrich=metric-metadata"))
	                .header("Content-Type", "application/json")
			        .header("Accept", "application/json")
			        //.header("Accept-Encoding", "gzip, deflate") // @TODO implement compressed response
			        .header("dt-client-context", "concurrency-client-context-concurrent")
			        .header("enforce-query-consumption-limit", "true")
			        .header("Authorization", "Bearer " + secretsProps.getProperty("apiToken"))
	                .timeout(Duration.ofSeconds(60))
	                .POST(HttpRequest.BodyPublishers.ofFile(Paths.get(DQL_JSON_FILENAME)))
	                .build();
	        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
	        if(response.statusCode() != 200) {
	        	System.err.println("Call to API did not return status code 200. " + response.statusCode());
	        	System.exit(-1);
	        }
			JsonReader rdr = Json.createReader(response.body());
			dqlResults = rdr.readObject();
			System.out.println("Retrieved data from API successfully");
		} catch (Exception e) {
			System.err.println("Can not load data from Dynatrace cloud API");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void loadDQLResults() {
		if(loadDQLResultsFromFileFlag)
			loadDQLResultsFromFile();
		else
			loadDQLResultsFromAPI();
	}
	
	private void saveDQLResultsToFile() {
		if(!saveDQLResultsToFileFlag)
			return;
		try {
			JsonWriter writer = Json.createWriter(new FileOutputStream(saveDQLResultsFileName));
			writer.write(dqlResults);
			writer.close();
			System.out.println("DQLResults saved sucessfully to file " + saveDQLResultsFileName);
		} catch (Exception e) {
			System.err.println("Problem saving the DQL outout file to " + saveDQLResultsFileName);
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void validateDQLResultsHealth() {
		String state = dqlResults.getString("state");
		int progress = dqlResults.getInt("progress");
		if(!"SUCCEEDED".equals(state) || progress != 100) {
			System.err.println("Data did not load properly, state=SUCCEEDED and progress=100 are expected.");
			System.err.println("state=" + state + ", progress=" + progress);
			System.exit(-1);
		}
	}
	
	private void printEventsCSVHeaders() {
		System.out.println();
		System.out.println(messagesProcessStartTimeField + "," + messagesProcessingTimeField + ",concurrency");
	}
	
	private void printStatsCSVHeaders() {
		System.out.println();
		System.out.println("Timeslot date-time,Event count,Max concurrency");
	}
	
	private void printCSVEvent(Event e) {
		System.out.println(e.getStartDateTime() + "," + e.getDurationInMs() + "," + e.getConcurrency());
	}
	
	private void printCSVTimeslotStatistics(TimeslotStatistics stats) {
		System.out.println(stats.getTimeslotStart() + "," + stats.getEventCount() + "," + stats.getMaxConcurrency());
	}
	
	private void parseResults() {
		if(eventsOutputFlag)
			printEventsCSVHeaders();
		ConcurrencyStateEngine engine = null;
		if(timeslotStatisticsOutputFlag) {
			engine = new ConcurrencyStateEngine(timeslotDurationInSeconds);
			statisticsArrayList = new ArrayList<TimeslotStatistics>(128);
		}
		else
			engine = new ConcurrencyStateEngine();
		JsonArray records = dqlResults.getJsonObject("result").getJsonArray("records");
		for(JsonObject record : records.getValuesAs(JsonObject.class)) {
			Event event = new Event(ZonedDateTime.parse(record.getString(messagesProcessStartTimeField)), Integer.parseInt(record.getString(messagesProcessingTimeField)));
			engine.receiveEvent(event);
			if(eventsOutputFlag)
				printCSVEvent(event);
			if(timeslotStatisticsOutputFlag) {
				if(engine.hasPreviousTimeslowStatistics()) {
					statisticsArrayList.add(engine.getPreviousTimeslotStatisticsAndClear());
				}
			}
		}
		if(timeslotStatisticsOutputFlag) {
			//fetch last stats
			statisticsArrayList.add(engine.getCurrentTimeslowStatistics());
			//print all stats
			printStatsCSVHeaders();
			for(TimeslotStatistics stats:statisticsArrayList)
				printCSVTimeslotStatistics(stats);
		}
		System.out.println();
		System.out.println("Engine max Concurrency: " + engine.getMaxConcurrency());
		JsonObject grail = dqlResults.getJsonObject("result").getJsonObject("metadata").getJsonObject("grail");
		System.out.println("Grail stats: scannedRecords:" + grail.getInt("scannedRecords") + ", scannedBytes:" + grail.getInt("scannedBytes"));
		JsonArray notifications = grail.getJsonArray("notifications");
		for(JsonObject notification : notifications.getValuesAs(JsonObject.class)) {
			System.out.println("Grail notification: " + notification.getString("severity") + ": " + notification.getString("message"));
		}
		System.out.println("Analysis complete.");
	}

	public static void main(String[] args) {
		Concurrency concurrency = new Concurrency();
		concurrency.initData(args);
		concurrency.validateInitData();
		concurrency.loadDQLResults();
		concurrency.saveDQLResultsToFile();
		concurrency.validateDQLResultsHealth();
		concurrency.parseResults();
	}

}
