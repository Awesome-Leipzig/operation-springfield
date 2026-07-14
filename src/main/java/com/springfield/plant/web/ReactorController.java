package com.springfield.plant.web;

import com.springfield.plant.model.Reactor;
import com.springfield.plant.service.ReactorService;
import com.springfield.plant.util.DateUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Reactor REST API.
 */
@RestController
@RequestMapping("/api/reactors")
public class ReactorController {

    private final ReactorService reactorService;

    public ReactorController(ReactorService reactorService) {
        this.reactorService = reactorService;
    }

    @GetMapping
    public List<Reactor> all() {
        return reactorService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reactor> byId(@PathVariable Long id) {
        return reactorService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Reactor create(@Valid @RequestBody Reactor reactor) {
        if (reactor.getLastInspection() == null) {
            reactor.setLastInspection(DateUtils.daysAgo(0));
        }
        return reactorService.save(reactor);
    }

    @GetMapping("/output")
    public String totalOutput() {
        return "Total online output: " + reactorService.totalOnlineOutputMw() + " MW";
    }

    @GetMapping("/overdue")
    public List<Reactor> overdue() {
        return reactorService.overdueForInspection(90);
    }

    @PostMapping("/{id}/inspect")
    public ResponseEntity<Reactor> inspect(@PathVariable Long id) {
        return reactorService.inspect(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
