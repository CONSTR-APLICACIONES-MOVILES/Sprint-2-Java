package com.example.parchapp.data;

import java.util.Locale;
import java.util.Map;

/** Backend alert DTO. It contains no presentation-only sample data. */
public final class Alert {
    private final String id;
    private final String activityId;
    private final String kind;
    private final String title;
    private final String description;
    private final String createdAt;
    private final boolean read;
    private final String response;

    private Alert(String id, String activityId, String kind, String title, String description,
                  String createdAt, boolean read, String response) {
        this.id = id;
        this.activityId = activityId;
        this.kind = kind;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.read = read;
        this.response = response;
    }

    public static Alert fromMap(Map<?, ?> data) {
        String id = string(data.get("id"));
        String activityId = string(data.get("studySessionId"));
        if (activityId.isEmpty() && "invitation".equals(string(data.get("kind")))) {
            activityId = id;
        }
        return new Alert(id, activityId, string(data.get("kind")), string(data.get("title")),
                string(data.get("description")), string(data.get("createdAt")),
                Boolean.TRUE.equals(data.get("isRead")), string(data.get("response")));
    }

    private static String string(Object value) {
        return value == null ? "" : value.toString();
    }

    public String getId() { return id; }
    public String getActivityId() { return activityId; }
    public String getKind() { return kind; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }
    public String getResponse() { return response; }

    public String getCategory() {
        switch (kind) {
            case "invitation": return "invitations";
            case "reminder":
            case "timeChanged": return "schedule";
            case "groupInvite": return "groups";
            default: return "groups";
        }
    }

    public String getPriority() {
        if ("reminder".equals(kind)) return "high";
        if ("invitation".equals(kind)) return "high";
        if ("timeChanged".equals(kind)) return "medium";
        return "low";
    }

    public String getTimeLabel() {
        if (createdAt.length() >= 16 && createdAt.contains("T")) {
            return createdAt.substring(11, 16);
        }
        return createdAt.isEmpty() ? "" : createdAt;
    }

    public String getDisplayKind() {
        if (kind.isEmpty()) return "Alert";
        return kind.substring(0, 1).toUpperCase(Locale.ROOT) + kind.substring(1);
    }
}
