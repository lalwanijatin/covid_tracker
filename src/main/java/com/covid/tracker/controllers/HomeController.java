package com.covid.tracker.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.covid.tracker.models.LocationStats;
import com.covid.tracker.services.CoronavirusDataService;

@Controller // Note this is not a rest controller. So what ever we return from the methods will point
			// to the template. This mapping from the name to an html file in templates will happen
			// because we have Thymeleaf.  
public class HomeController {
	
	@Autowired
	private CoronavirusDataService coronavirusDataService;
	
	@GetMapping("/")
	// model parameter is passed by the spring here. So the purpose is that you can insert values in this
	// model and then this model will be available in the template name that you are returning.
	// So in this example what ever you will add to this model will be available for use in the home.html
	public String home(Model model) { 
		List<LocationStats> locationStats = coronavirusDataService.getAllstats();
		int totalCases = 0;
		int totalDiff = 0;
		for(LocationStats ls : locationStats) {
			totalCases += ls.getLatestTotalCases();
			totalDiff += ls.getDiffFromPrevDay();
		}
		model.addAttribute("locationStats", locationStats);
		model.addAttribute("totalCases", totalCases);
		model.addAttribute("totalDiff", totalDiff);
		return "home";
	}
}
