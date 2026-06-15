package de.hitec.nhplus.model;

import de.hitec.nhplus.utils.DateConverter;

import java.time.LocalDate;
import java.time.LocalTime;

public class Treatment {
    private long tid;
    private final long pid;
    private LocalDate date;
    private LocalTime begin;
    private LocalTime end;
    private String description;
    private String remarks;
    private long caregiverId;
    private String caregiverName;
    private String caregiverTelephone;

    // Neue Behandlung ohne Pfleger
    public Treatment(long pid, LocalDate date, LocalTime begin,
                     LocalTime end, String description, String remarks) {
        this(pid, date, begin, end, description, remarks, 0);
    }

    // Neue Behandlung mit Pfleger
    public Treatment(long pid, LocalDate date, LocalTime begin,
                     LocalTime end, String description, String remarks, long caregiverId) {
        this.pid = pid;
        this.date = date;
        this.begin = begin;
        this.end = end;
        this.description = description;
        this.remarks = remarks;
        this.caregiverId = caregiverId;
        this.caregiverName = "";
        this.caregiverTelephone = "";
    }

    // Aus der Datenbank geladen, ohne Pfleger-Infos
    public Treatment(long tid, long pid, LocalDate date, LocalTime begin,
                     LocalTime end, String description, String remarks) {
        this(tid, pid, date, begin, end, description, remarks, 0, "", "");
    }

    // Aus der Datenbank geladen, mit Pfleger-Infos (aus JOIN)
    public Treatment(long tid, long pid, LocalDate date, LocalTime begin,
                     LocalTime end, String description, String remarks,
                     long caregiverId, String caregiverName, String caregiverTelephone) {
        this.tid = tid;
        this.pid = pid;
        this.date = date;
        this.begin = begin;
        this.end = end;
        this.description = description;
        this.remarks = remarks;
        this.caregiverId = caregiverId;
        this.caregiverName = caregiverName;
        this.caregiverTelephone = caregiverTelephone;
    }

    public long getTid() { return tid; }
    public long getPid() { return pid; }

    public String getDate() { return date.toString(); }
    public String getBegin() { return begin.toString(); }
    public String getEnd() { return end.toString(); }

    public void setDate(String date) { this.date = DateConverter.convertStringToLocalDate(date); }
    public void setBegin(String begin) { this.begin = DateConverter.convertStringToLocalTime(begin); }
    public void setEnd(String end) { this.end = DateConverter.convertStringToLocalTime(end); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public long getCaregiverId() { return caregiverId; }
    public void setCaregiverId(long caregiverId) { this.caregiverId = caregiverId; }

    public String getCaregiverName() { return caregiverName; }
    public String getCaregiverTelephone() { return caregiverTelephone; }

    public String toString() {
        return "\nBehandlung" + "\nTID: " + this.tid +
                "\nPID: " + this.pid +
                "\nDate: " + this.date +
                "\nBegin: " + this.begin +
                "\nEnd: " + this.end +
                "\nDescription: " + this.description +
                "\nRemarks: " + this.remarks + "\n";
    }
}
