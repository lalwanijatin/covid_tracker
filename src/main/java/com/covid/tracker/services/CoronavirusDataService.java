package com.covid.tracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.covid.tracker.models.LocationStats;

@Service
public class CoronavirusDataService {
	private String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
	
	
	private List<LocationStats> allstats = new ArrayList<>(); // store current stats in main memory

	public List<LocationStats> getAllstats() {
		return allstats;
	}

	@PostConstruct // This basically tells spring that when you construct the instance of this service
					// after it's done, just execute this method.
	@Scheduled(fixedDelay = 500000)// We also want this method to run every regularly. Otherwise we would have
									// start the application again to fetch the latest data.
	public void fetchVirusData() throws IOException, InterruptedException {
		
		List<LocationStats> newStats = new ArrayList<>();
		
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(VIRUS_DATA_URL))
				.build();
		
		// send request. First argument is request. Second is "What to do with the body". So here in the
		// second parameter we have asked it to take the body and return it as a string.
		HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		
		// To parse this csv body we will use apache-commons library
		
		// To parse the csv text apache-commons need an instance of Reader.
		// StringReader is an instance of Reader
		StringReader csvBodyReader = new StringReader(httpResponse.body());
		
		// Copy pasted from the apache-commons user guide
		// https://commons.apache.org/proper/commons-csv/user-guide.html 
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
		for (CSVRecord record : records) {
			String state = record.get("Province/State");
			String country = record.get("Country/Region");
			
			int colSize = record.size();
			String lastColValue = record.get(colSize-1);
			int latestTotalCases = Integer.parseInt(lastColValue);
			
			String secondLastColValue = record.get(colSize-2);
			int prevDayTotalCases = Integer.parseInt(secondLastColValue);
			int delta = latestTotalCases - prevDayTotalCases;
			
			newStats.add(new LocationStats(state, country, latestTotalCases, delta));
		}
		
		this.allstats = newStats;
	}
}
