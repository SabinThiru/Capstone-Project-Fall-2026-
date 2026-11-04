package com.campuspath.backend.schedule;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.campuspath.backend.schedule.ScheduleDtos.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService schedule;

    public ScheduleController(ScheduleService schedule) {
        this.schedule = schedule;
    }

    @GetMapping("/events")
    public List<EventResponse> listEvents(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return schedule.listEvents(jwt.getSubject(), from, to);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody EventRequest body) {
        return schedule.createEvent(jwt.getSubject(), body);
    }

    @PutMapping("/events/{id}")
    public EventResponse updateEvent(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                     @Valid @RequestBody EventRequest body) {
        return schedule.updateEvent(jwt.getSubject(), id, body);
    }

    @DeleteMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        schedule.deleteEvent(jwt.getSubject(), id);
    }

    @GetMapping("/commitments")
    public List<CommitmentResponse> listCommitments(@AuthenticationPrincipal Jwt jwt) {
        return schedule.listCommitments(jwt.getSubject());
    }

    @PostMapping("/commitments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommitmentResponse createCommitment(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody CommitmentRequest body) {
        return schedule.createCommitment(jwt.getSubject(), body);
    }

    @PutMapping("/commitments/{id}")
    public CommitmentResponse updateCommitment(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                               @Valid @RequestBody CommitmentRequest body) {
        return schedule.updateCommitment(jwt.getSubject(), id, body);
    }

    @DeleteMapping("/commitments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommitment(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        schedule.deleteCommitment(jwt.getSubject(), id);
    }

    @GetMapping("/terms")
    public List<TermResponse> listTerms(@AuthenticationPrincipal Jwt jwt) {
        return schedule.listTerms(jwt.getSubject());
    }

    @PostMapping("/terms")
    @ResponseStatus(HttpStatus.CREATED)
    public TermResponse createTerm(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TermRequest body) {
        return schedule.createTerm(jwt.getSubject(), body);
    }

    @GetMapping("/generated")
    public List<GeneratedResponse> listGenerated(@AuthenticationPrincipal Jwt jwt, @RequestParam Long termId) {
        return schedule.listGenerated(jwt.getSubject(), termId);
    }

    @PostMapping("/generated")
    @ResponseStatus(HttpStatus.CREATED)
    public GeneratedResponse createGenerated(@AuthenticationPrincipal Jwt jwt,
                                             @Valid @RequestBody GeneratedRequest body) {
        return schedule.createGenerated(jwt.getSubject(), body);
    }

    @PostMapping("/generated/{id}/select")
    public GeneratedResponse selectGenerated(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return schedule.selectGenerated(jwt.getSubject(), id);
    }

    @DeleteMapping("/generated/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGenerated(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        schedule.deleteGenerated(jwt.getSubject(), id);
    }
}