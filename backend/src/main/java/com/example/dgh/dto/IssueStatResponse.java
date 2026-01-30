package com.example.dgh.dto;

public class IssueStatResponse {
    private String issue;
    private long count;

    public IssueStatResponse(String issue, long count) {
        this.issue = issue;
        this.count = count;
    }

    public String getIssue() {
        return issue;
    }

    public long getCount() {
        return count;
    }
}
