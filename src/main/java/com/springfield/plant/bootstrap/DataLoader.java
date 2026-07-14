package com.springfield.plant.bootstrap;

import com.springfield.plant.model.Employee;
import com.springfield.plant.model.Reactor;
import com.springfield.plant.model.SafetyIncident;
import com.springfield.plant.service.EmployeeService;
import com.springfield.plant.service.IncidentService;
import com.springfield.plant.service.ReactorService;
import com.springfield.plant.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the plant with data. All incidents are historically accurate.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final ReactorService reactorService;
    private final IncidentService incidentService;
    private final EmployeeService employeeService;

    public DataLoader(ReactorService reactorService, IncidentService incidentService, EmployeeService employeeService) {
        this.reactorService = reactorService;
        this.incidentService = incidentService;
        this.employeeService = employeeService;
    }

    @Override
    public void run(String... args) {
        // ddl-auto is `update` in production (see application.properties) so the
        // Postgres schema persists across restarts/replica scale-ups -- without
        // this guard, every cold start would insert a fresh set of duplicate seed
        // rows on top of whatever's already there.
        if (!reactorService.findAll().isEmpty()) {
            log.info("Sector 7G Safety Ledger already seeded ({} reactors) -- skipping DataLoader.",
                    reactorService.findAll().size());
            return;
        }

        Reactor core1 = reactorService.save(new Reactor("Old Bessie", "7G", "ONLINE", 480, DateUtils.daysAgo(200)));
        Reactor core2 = reactorService.save(new Reactor("Core Blimey", "7G", "ONLINE", 512, DateUtils.daysAgo(45)));
        Reactor core3 = reactorService.save(new Reactor("The Inconvenience", "6F", "MELTDOWN-ISH", 730, DateUtils.daysAgo(730)));
        reactorService.save(new Reactor("Backup Bart", "3B", "OFFLINE", 0, null));

        employeeService.save(new Employee("Homer Simpson", "Safety Inspector", "TOP SECRET (misfiled)"));
        employeeService.save(new Employee("Carl Carlson", "Shift Supervisor", "SECRET"));
        employeeService.save(new Employee("Lenny Leonard", "Reactor Technician", "SECRET"));
        employeeService.save(new Employee("Waylon Smithers", "Executive Assistant", "ALL OF THEM"));

        incidentService.report(new SafetyIncident(core1, "control rod used as back scratcher", 2,
                "Homer Simpson", DateUtils.daysAgo(90), 6));
        incidentService.report(new SafetyIncident(core3, "core temperature exceeded 'the red bit'", 5,
                "Carl Carlson", DateUtils.daysAgo(30), 0));
        incidentService.report(new SafetyIncident(core2, "coolant replaced with duff beer (allegedly)", 4,
                "Lenny Leonard", DateUtils.daysAgo(14), 12));
        incidentService.report(new SafetyIncident(core1, "safety manual used to prop open vent door", 3,
                "Homer Simpson", DateUtils.daysAgo(7), 3));
        incidentService.report(new SafetyIncident(core3, "glowing rat spotted near turbine hall", 4,
                "Waylon Smithers", DateUtils.daysAgo(2), 1));

        log.info("Sector 7G Safety Ledger loaded. {} reactors, {} incidents. Everything is fine.",
                reactorService.findAll().size(), incidentService.findAll().size());
    }
}
