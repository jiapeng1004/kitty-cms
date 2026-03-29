package icu.jiapeng.qk.model;

public record EventReportResult(boolean ok) {
    public static final EventReportResult OK = new EventReportResult(true);
}
