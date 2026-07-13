package com.springfield.plant.web;

import com.springfield.plant.service.IncidentService;
import com.springfield.plant.service.ReactorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The plant dashboard — Thymeleaf, one page, zero JavaScript budget.
 */
@Controller
public class DashboardController {

    private final ReactorService reactorService;
    private final IncidentService incidentService;

    public DashboardController(ReactorService reactorService, IncidentService incidentService) {
        this.reactorService = reactorService;
        this.incidentService = incidentService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("banner", reactorService.statusBanner());
        model.addAttribute("reactors", reactorService.findAll());
        model.addAttribute("incidents", incidentService.findAll());
        model.addAttribute("donuts", incidentService.totalDonuts());
        return "dashboard";
    }
}
