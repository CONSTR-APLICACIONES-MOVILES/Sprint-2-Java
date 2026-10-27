package com.example.parchapp.data;

import java.util.Map;

public final class ProfileData {
    private final String id;
    private final String name;
    private final String program;
    private final String campus;
    private final String university;
    private final String studentId;
    private final int semester;
    private final int year;
    private final String academicLevel;
    private final String status;
    private final int activeGroups;
    private final int plansCompleted;
    private final Double onTimeRate;
    private final boolean verified;

    private ProfileData(String id, String name, String program, String campus, String university,
                        String studentId, int semester, int year, String academicLevel,
                        String status, int activeGroups, int plansCompleted, Double onTimeRate,
                        boolean verified) {
        this.id = id;
        this.name = name;
        this.program = program;
        this.campus = campus;
        this.university = university;
        this.studentId = studentId;
        this.semester = semester;
        this.year = year;
        this.academicLevel = academicLevel;
        this.status = status;
        this.activeGroups = activeGroups;
        this.plansCompleted = plansCompleted;
        this.onTimeRate = onTimeRate;
        this.verified = verified;
    }

    public static ProfileData fromMap(Map<?, ?> map) {
        return new ProfileData(string(map, "id"), string(map, "name"), string(map, "program"),
                string(map, "campus"), string(map, "university"), string(map, "studentId"),
                integer(map, "semester"), integer(map, "year"), string(map, "academicLevel"),
                string(map, "status"), integer(map, "activeGroups"), integer(map, "plansCompleted"),
                number(map.get("onTimeRate")), Boolean.TRUE.equals(map.get("verified")));
    }

    private static String string(Map<?, ?> map, String key) { return map.get(key) == null ? "" : map.get(key).toString(); }
    private static int integer(Map<?, ?> map, String key) { return map.get(key) instanceof Number ? ((Number) map.get(key)).intValue() : 0; }
    private static Double number(Object value) { return value instanceof Number ? ((Number) value).doubleValue() : null; }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getProgram() { return program; }
    public String getCampus() { return campus; }
    public String getUniversity() { return university; }
    public String getStudentId() { return studentId; }
    public int getSemester() { return semester; }
    public int getYear() { return year; }
    public String getAcademicLevel() { return academicLevel; }
    public String getStatus() { return status; }
    public int getActiveGroups() { return activeGroups; }
    public int getPlansCompleted() { return plansCompleted; }
    public Double getOnTimeRate() { return onTimeRate; }
    public boolean isVerified() { return verified; }

    public String getInitials() {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return "?";
        return parts[0].substring(0, 1).toUpperCase()
                + (parts.length > 1 ? parts[parts.length - 1].substring(0, 1).toUpperCase() : "");
    }
}
