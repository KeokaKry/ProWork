package com.worktrcker.app.controller;

import com.worktrcker.app.model.Position;
import com.worktrcker.app.repository.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/positions")
@CrossOrigin(origins = "*")
public class PositionController {

    @Autowired
    private PositionRepository positionRepository;

    @GetMapping
    public ResponseEntity<List<Position>> getAllPositions() {
        return ResponseEntity.ok(positionRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Position> createPosition(@RequestBody Position position) {
        return ResponseEntity.ok(positionRepository.save(position));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Position> updatePosition(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return positionRepository.findById(id)
            .map(position -> {
                if (updates.containsKey("name")) {
                    position.setName((String) updates.get("name"));
                }
                if (updates.containsKey("hourlyRate")) {
                    position.setHourlyRate(Double.valueOf(updates.get("hourlyRate").toString()));
                }
                return ResponseEntity.ok(positionRepository.save(position));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        positionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}