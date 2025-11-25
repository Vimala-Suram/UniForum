package edu.northeastern.uniforum.forum.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

import com.google.api.services.calendar.model.*;
import com.google.api.services.calendar.Calendar;
import com.google.api.client.util.DateTime;

import edu.northeastern.uniforum.forum.model.User;

public class MeetingController {

    @FXML private TextField subjectField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;

    private User loggedInUser;
    private User targetUser;

    /**
     * Sets the logged-in user
     */
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    /**
     * Sets the target user (the person being scheduled with)
     */
    public void setTargetUser(User user) {
        this.targetUser = user;
    }

    /**
     * Sets the attendee email (pre-fills the email field)
     */
    public void setAttendeeEmail(String email) {
        if (emailField != null && email != null) {
            emailField.setText(email);
        }
    }

    @FXML
    public void handleScheduleMeeting() {
        // Clear previous status
        if (statusLabel != null) {
            statusLabel.setText("");
            statusLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 14px;");
        }

        try {
            // ---- Read Input ----
            String subject = subjectField != null ? subjectField.getText().trim() : "";
            LocalDate date = datePicker != null ? datePicker.getValue() : null;
            String timeString = timeField != null ? timeField.getText().trim() : "";
            String email = emailField != null ? emailField.getText().trim() : "";

            // ---- Validate ----
            if (subject.isEmpty()) {
                showError("Please enter a meeting subject!");
                return;
            }
            
            if (date == null) {
                showError("Please select a date!");
                return;
            }
            
            if (timeString.isEmpty()) {
                showError("Please enter a time (HH:MM format)!");
                return;
            }
            
            if (email.isEmpty() || !isValidEmail(email)) {
                showError("Please enter a valid email address!");
                return;
            }

            // Parse time - support both HH:MM and HH:MM:SS formats
            LocalTime time;
            try {
                if (timeString.matches("\\d{1,2}:\\d{2}")) {
                    // HH:MM format
                    time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("H:mm"));
                } else if (timeString.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
                    // HH:MM:SS format
                    time = LocalTime.parse(timeString);
                } else {
                    showError("Time format invalid. Use HH:MM (e.g., 14:30)");
                    return;
                }
            } catch (DateTimeParseException e) {
                showError("Time format invalid. Use HH:MM (e.g., 14:30)");
                return;
            }

            // Check if date/time is in the past
            LocalDateTime startDT = LocalDateTime.of(date, time);
            if (startDT.isBefore(LocalDateTime.now())) {
                showError("Cannot schedule meetings in the past!");
                return;
            }

            LocalDateTime endDT = startDT.plusHours(1); // Default 1-hour meeting

            // Show processing message
            if (statusLabel != null) {
                statusLabel.setText("Creating meeting...");
                statusLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-size: 14px;");
            }

            // Create event in a separate thread to avoid blocking UI
            new Thread(() -> {
                try {
                    String meetLink = createGoogleMeet(subject, startDT, endDT, email);
                    
                    Platform.runLater(() -> {
                        if (statusLabel != null) {
                            statusLabel.setText("✓ Meeting created successfully!\nGoogle Meet: " + meetLink);
                            statusLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12px;");
                        }
                        
                        // Show success dialog with meet link
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Meeting Scheduled");
                        alert.setHeaderText("Meeting created successfully!");
                        alert.setContentText("Google Meet Link:\n" + meetLink + 
                                           "\n\nAn email invitation has been sent to " + email);
                        alert.showAndWait();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        ex.printStackTrace();
                        showError("Failed to create meeting: " + ex.getMessage());
                    });
                }
            }).start();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error: " + ex.getMessage());
        }
    }

    /**
     * Validates email format
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Shows an error message in the status label
     */
    private void showError(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: #f44336; -fx-font-size: 14px;");
        }
    }

    /**
     * Creates Google Calendar Event with Google Meet link
     */
    private String createGoogleMeet(String summary,
                                    LocalDateTime start,
                                    LocalDateTime end,
                                    String attendeeEmail) throws Exception {

        Calendar service = GoogleCalendarService.getInstance();

        // Create event object
        Event event = new Event();
        event.setSummary(summary);
        event.setDescription("Meeting scheduled via UniForum");

        // Convert LocalDateTime to Google Calendar DateTime (RFC 3339 format)
        // Google Calendar API requires timezone-aware datetime
        ZoneId zoneId = ZoneId.systemDefault();
        long startMillis = start.atZone(zoneId).toInstant().toEpochMilli();
        long endMillis = end.atZone(zoneId).toInstant().toEpochMilli();

        DateTime startDateTime = new DateTime(startMillis);
        DateTime endDateTime = new DateTime(endMillis);

        EventDateTime startEventDateTime = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(zoneId.getId());
        
        EventDateTime endEventDateTime = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(zoneId.getId());

        event.setStart(startEventDateTime);
        event.setEnd(endEventDateTime);

        // Add attendees
        EventAttendee attendee = new EventAttendee().setEmail(attendeeEmail);
        
        // Add logged-in user as attendee if available
        List<EventAttendee> attendees = new java.util.ArrayList<>();
        attendees.add(attendee);
        
        if (loggedInUser != null && loggedInUser.getEmail() != null && 
            !loggedInUser.getEmail().equals(attendeeEmail)) {
            attendees.add(new EventAttendee().setEmail(loggedInUser.getEmail()));
        }
        
        event.setAttendees(attendees);

        // Enable Google Meet conference
        ConferenceData conferenceData = new ConferenceData();
        CreateConferenceRequest createRequest = new CreateConferenceRequest();
        createRequest.setRequestId("meet-" + System.currentTimeMillis());
        createRequest.setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet"));
        
        conferenceData.setCreateRequest(createRequest);
        event.setConferenceData(conferenceData);

        // Insert event into Google Calendar with conference data
        Event createdEvent = service.events()
                .insert("primary", event)
                .setConferenceDataVersion(1)
                .setSendUpdates("all") // Send email invitations to all attendees
                .execute();

        // Extract Google Meet link from the created event
        String meetLink = null;
        if (createdEvent.getConferenceData() != null && 
            createdEvent.getConferenceData().getEntryPoints() != null) {
            for (EntryPoint entryPoint : createdEvent.getConferenceData().getEntryPoints()) {
                if ("video".equals(entryPoint.getEntryPointType())) {
                    meetLink = entryPoint.getUri();
                    break;
                }
            }
        }
        
        // Fallback to hangoutLink if entryPoints not available
        if (meetLink == null || meetLink.isEmpty()) {
            meetLink = createdEvent.getHangoutLink();
        }

        if (meetLink == null || meetLink.isEmpty()) {
            throw new Exception("Failed to generate Google Meet link");
        }

        return meetLink;
    }
}