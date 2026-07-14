package com.springfield.plant.bootstrap;

import com.springfield.plant.model.Employee;
import com.springfield.plant.model.Reactor;
import com.springfield.plant.model.SafetyIncident;
import com.springfield.plant.repository.EmployeeRepository;
import com.springfield.plant.repository.IncidentRepository;
import com.springfield.plant.repository.ReactorRepository;
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

    private final ReactorRepository reactors;
    private final IncidentRepository incidents;
    private final EmployeeRepository employees;

    public DataLoader(ReactorRepository reactors, IncidentRepository incidents, EmployeeRepository employees) {
        this.reactors = reactors;
        this.incidents = incidents;
        this.employees = employees;
    }

    @Override
    public void run(String... args) {
        Reactor core1 = reactors.save(new Reactor("Old Bessie", "7G", "ONLINE", 480, DateUtils.daysAgo(200)));
        Reactor core2 = reactors.save(new Reactor("Core Blimey", "7G", "ONLINE", 512, DateUtils.daysAgo(45)));
        Reactor core3 = reactors.save(new Reactor("The Inconvenience", "6F", "MELTDOWN-ISH", 730, DateUtils.daysAgo(730)));
        reactors.save(new Reactor("Backup Bart", "3B", "OFFLINE", 0, null));

        employees.save(new Employee("Homer Simpson", "Safety Inspector", "TOP SECRET (misfiled)"));
        employees.save(new Employee("Carl Carlson", "Shift Supervisor", "SECRET"));
        employees.save(new Employee("Lenny Leonard", "Reactor Technician", "SECRET"));
        employees.save(new Employee("Waylon Smithers", "Executive Assistant", "ALL OF THEM"));

        incidents.save(new SafetyIncident(core1, "control rod used as back scratcher", 2,
                "Homer Simpson", DateUtils.daysAgo(90), 6));
        incidents.save(new SafetyIncident(core3, "core temperature exceeded 'the red bit'", 5,
                "Carl Carlson", DateUtils.daysAgo(30), 0));
        incidents.save(new SafetyIncident(core2, "coolant replaced with duff beer (allegedly)", 4,
                "Lenny Leonard", DateUtils.daysAgo(14), 12));
        incidents.save(new SafetyIncident(core1, "safety manual used to prop open vent door", 3,
                "Homer Simpson", DateUtils.daysAgo(7), 3));
        incidents.save(new SafetyIncident(core3, "glowing rat spotted near turbine hall", 4,
                "Waylon Smithers", DateUtils.daysAgo(2), 1));

        log.info("Sector 7G Safety Ledger loaded. {} reactors, {} incidents. Everything is fine.",
                reactors.count(), incidents.count());
    }
}
