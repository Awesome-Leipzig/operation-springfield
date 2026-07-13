package com.springfield.plant.service;

import com.springfield.plant.model.Reactor;
import com.springfield.plant.repository.ReactorRepository;
import com.springfield.plant.util.DateUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Reactor business logic.
 * ☢️ LEGACY ALERT: manual loops instead of streams, boxing with `new Integer(...)`
 * (deprecated since Java 9, removed-ish in modern JDKs), StringBuffer abuse.
 */
@Service
public class ReactorService {

    private final ReactorRepository reactorRepository;

    public ReactorService(ReactorRepository reactorRepository) {
        this.reactorRepository = reactorRepository;
    }

    public List<Reactor> findAll() {
        return reactorRepository.findAll();
    }

    public Reactor findById(Long id) {
        return reactorRepository.findById(id).orElse(null); // ☢️ null-happy API
    }

    public Reactor save(Reactor reactor) {
        return reactorRepository.save(reactor);
    }

    /** Total thermal output across all ONLINE reactors, the 1998 way. */
    public Integer totalOnlineOutputMw() {
        List<Reactor> online = reactorRepository.findByStatus("ONLINE");
        Integer total = new Integer(0); // ☢️ deprecated boxing ceremony
        for (int i = 0; i < online.size(); i++) {
            Reactor r = online.get(i);
            if (r.getThermalOutputMw() != null) {
                total = new Integer(total.intValue() + r.getThermalOutputMw().intValue());
            }
        }
        return total;
    }

    /** Reactors that have not been inspected within the given number of days. */
    public List<Reactor> overdueForInspection(int maxDays) {
        List<Reactor> result = new ArrayList<Reactor>();
        Date cutoff = DateUtils.daysAgo(maxDays);
        List<Reactor> all = reactorRepository.findAll();
        for (int i = 0; i < all.size(); i++) {
            Reactor r = all.get(i);
            if (r.getLastInspection() == null || r.getLastInspection().before(cutoff)) {
                result.add(r);
            }
        }
        return result;
    }

    /** Plant status banner, lovingly assembled with StringBuffer. */
    public String statusBanner() {
        StringBuffer sb = new StringBuffer(); // ☢️ StringBuffer in single-threaded code
        sb.append("SNPP STATUS :: ");
        sb.append(reactorRepository.findByStatus("ONLINE").size()).append(" online / ");
        sb.append(reactorRepository.findByStatus("MELTDOWN-ISH").size()).append(" melting / ");
        sb.append(totalOnlineOutputMw()).append(" MW total. Everything is fine.");
        return sb.toString();
    }
}
