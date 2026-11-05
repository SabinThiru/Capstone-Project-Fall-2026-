package com.campuspath.backend.schedule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campuspath.backend.common.ApiException;
import com.campuspath.backend.schedule.ScheduleDtos.*;
import com.campuspath.backend.user.CurrentUserService;

@Service
public class ScheduleService {

    private static final Map<String, String> DEFAULT_COLORS = Map.of(
            "class", "#66b366",
            "work", "#88a9c2",
            "personal", "#e8b66e",
            "extracurricular", "#b48ad6",
            "other", "#4287f5");

    private final CurrentUserService currentUser;
    private final ScheduleEventRepository events;
    private final WeeklyCommitmentRepository commitments;
    private final TermRepository terms;
    private final GeneratedScheduleRepository generated;
    private final SchedulePreferencesRepository preferences;

    public ScheduleService(CurrentUserService currentUser, ScheduleEventRepository events,
                           WeeklyCommitmentRepository commitments, TermRepository terms,
                           GeneratedScheduleRepository generated, SchedulePreferencesRepository preferences) {
        this.currentUser = currentUser;
        this.events = events;
        this.commitments = commitments;
        this.terms = terms;
        this.generated = generated;
        this.preferences = preferences;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> listEvents(String sub, LocalDate from, LocalDate to) {
        Integer userId = currentUser.requireId(sub);
        List<ScheduleEvent> found = (from != null && to != null)
                ? events.findByUserIdAndEventDateBetweenOrderByEventDateAscStartTimeAsc(userId, from, to)
                : events.findByUserIdOrderByEventDateAscStartTimeAsc(userId);
        return found.stream().map(EventResponse::from).toList();
    }

    @Transactional
    public EventResponse createEvent(String sub, EventRequest req) {
        ScheduleEvent event = new ScheduleEvent(currentUser.requireId(sub));
        applyEvent(event, req);
        return EventResponse.from(events.save(event));
    }

    @Transactional
    public EventResponse updateEvent(String sub, Long id, EventRequest req) {
        ScheduleEvent event = findEvent(sub, id);
        applyEvent(event, req);
        return EventResponse.from(event);
    }

    @Transactional
    public void deleteEvent(String sub, Long id) {
        events.delete(findEvent(sub, id));
    }

    private void applyEvent(ScheduleEvent event, EventRequest req) {
        requireEndAfterStart(req.startTime(), req.endTime());
        event.update(req.title().trim(), req.eventType(), req.eventDate(), req.startTime(), req.endTime(),
                blankToNull(req.location()), blankToNull(req.notes()));
    }

    private ScheduleEvent findEvent(String sub, Long id) {
        return events.findByIdAndUserId(id, currentUser.requireId(sub))
                .orElseThrow(() -> ApiException.notFound("Event not found"));
    }

    @Transactional(readOnly = true)
    public List<CommitmentResponse> listCommitments(String sub) {
        return commitments.findByUserIdOrderByDayOfWeekAscStartTimeAsc(longId(sub))
                .stream().map(CommitmentResponse::from).toList();
    }

    @Transactional
    public CommitmentResponse createCommitment(String sub, CommitmentRequest req) {
        WeeklyCommitment commitment = new WeeklyCommitment(longId(sub));
        applyCommitment(commitment, req);
        return CommitmentResponse.from(commitments.save(commitment));
    }

    @Transactional
    public CommitmentResponse updateCommitment(String sub, Long id, CommitmentRequest req) {
        WeeklyCommitment commitment = findCommitment(sub, id);
        applyCommitment(commitment, req);
        return CommitmentResponse.from(commitment);
    }

    @Transactional
    public void deleteCommitment(String sub, Long id) {
        commitments.delete(findCommitment(sub, id));
    }

    private void applyCommitment(WeeklyCommitment c, CommitmentRequest req) {
        requireEndAfterStart(req.startTime(), req.endTime());
        requireDateOrder(req.startDate(), req.endDate());
        String color = (req.color() == null || req.color().isBlank())
                ? DEFAULT_COLORS.get(req.commitmentType())
                : req.color().toLowerCase();
        c.update(req.title().trim(), req.commitmentType(), req.dayOfWeek(), req.startTime(), req.endTime(),
                req.startDate(), req.endDate(), color);
    }

    private WeeklyCommitment findCommitment(String sub, Long id) {
        return commitments.findByIdAndUserId(id, longId(sub))
                .orElseThrow(() -> ApiException.notFound("Commitment not found"));
    }

    @Transactional(readOnly = true)
    public List<TermResponse> listTerms(String sub) {
        return terms.findByUserIdOrderByStartDateDesc(currentUser.requireId(sub))
                .stream().map(TermResponse::from).toList();
    }

    @Transactional
    public TermResponse createTerm(String sub, TermRequest req) {
        Integer userId = currentUser.requireId(sub);
        requireDateOrder(req.startDate(), req.endDate());
        String name = req.name().trim();
        if (terms.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw ApiException.conflict("You already have a term called " + name);
        }
        return TermResponse.from(terms.save(new Term(userId, name, req.startDate(), req.endDate())));
    }

    @Transactional(readOnly = true)
    public List<GeneratedResponse> listGenerated(String sub, Long termId) {
        Integer userId = currentUser.requireId(sub);
        findTerm(userId, termId);
        return generated.findByUserIdAndTermIdOrderByIdAsc(userId, termId)
                .stream().map(GeneratedResponse::from).toList();
    }

    @Transactional
    public GeneratedResponse createGenerated(String sub, GeneratedRequest req) {
        Integer userId = currentUser.requireId(sub);
        Term term = findTerm(userId, req.termId());
        BigDecimal score = null;
        GeneratedSchedule saved = generated.save(
                new GeneratedSchedule(userId, term.getId(), req.scheduleName().trim(), score));
        return GeneratedResponse.from(saved);
    }

    @Transactional
    public GeneratedResponse selectGenerated(String sub, Long id) {
        Integer userId = longId(sub);
        GeneratedSchedule chosen = findGenerated(userId, id);
        for (GeneratedSchedule other : generated.findByUserIdAndTermIdOrderByIdAsc(userId, chosen.getTermId())) {
            other.setSelected(false);
        }
        generated.flush();
        chosen.setSelected(true);
        return GeneratedResponse.from(chosen);
    }

    @Transactional
    public void deleteGenerated(String sub, Long id) {
        Integer userId = longId(sub);
        generated.delete(findGenerated(userId, id));
    }

    private BigDecimal scoreTerm(Integer userId, Term term) {
        List<ScheduleScorer.Slot> slots = commitments.findByUserIdOrderByDayOfWeekAscStartTimeAsc(userId)
                .stream()
                .filter(c -> c.getDayOfWeek() != null
                        && !c.getStartDate().isAfter(term.getEndDate())
                        && (c.getEndDate() == null || !c.getEndDate().isBefore(term.getStartDate())))
                .map(c -> new ScheduleScorer.Slot(c.getCommitmentType(), c.getDayOfWeek(),
                        c.getStartTime(), c.getEndTime()))
                .toList();
        SchedulePreferences p = preferences.findByUserId(userId)
                .orElseGet(() -> SchedulePreferences.defaults(userId));
        return ScheduleScorer.score(slots, new ScheduleScorer.Preferences(
                p.getEarliestClassTime(), p.getLatestClassTime(), new HashSet<>(p.getPreferredDayNumbers()),
                p.isMinimizeGaps(), p.isMinimizeCampusDays()));
    }

    private Term findTerm(Integer userId, Long termId) {
        return terms.findByIdAndUserId(termId, userId)
                .orElseThrow(() -> ApiException.notFound("Term not found"));
    }

    private GeneratedSchedule findGenerated(Integer userId, Long id) {
        return generated.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Schedule option not found"));
    }

    @Transactional(readOnly = true)
    public PreferencesResponse getPreferences(String sub) {
        return preferences.findByUserId(longId(sub))
                .map(PreferencesResponse::from)
                .orElseThrow(() -> ApiException.notFound("No schedule preferences saved yet"));
    }

    @Transactional
    public PreferencesResponse savePreferences(String sub, PreferencesRequest req) {
        Integer userId = longId(sub);
        if (!req.latestClassTime().isAfter(req.earliestClassTime())) {
            throw ApiException.badRequest("The earliest class time must be before the latest.");
        }
        SchedulePreferences p = preferences.findByUserId(userId).orElseGet(() -> new SchedulePreferences(userId));
        p.update(req.earliestClassTime(), req.latestClassTime(), req.maxCredits(), req.preferredDays(),
                req.minimizeGaps(), req.minimizeCampusDays());
        return PreferencesResponse.from(preferences.save(p));
    }

    private Integer longId(String sub) {
        return currentUser.requireId(sub);
    }
    private static void requireEndAfterStart(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw ApiException.badRequest("End time must be after start time on the same day.");
        }
    }

    private static void requireDateOrder(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw ApiException.badRequest("End date must be on or after start date.");
        }
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}