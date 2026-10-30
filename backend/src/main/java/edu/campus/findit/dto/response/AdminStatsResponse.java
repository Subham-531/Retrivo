package edu.campus.findit.dto.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminStatsResponse {
    private long totalUsers;
    private long totalStudents;
    private long totalLost;
    private long totalFound;
    private long totalRecovered;
    private long totalMatches;
    private long pendingClaims;
    private double recoveryRatePercentage;
    private Map<String, Long> categoryDistribution = new HashMap<>();
    private List<MonthlyTrend> monthlyTrends = new ArrayList<>();

    public static class MonthlyTrend {
        private String month;
        private long reported;
        private long recovered;

        public MonthlyTrend() {}
        public MonthlyTrend(String month, long reported, long recovered) {
            this.month = month;
            this.reported = reported;
            this.recovered = recovered;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public long getReported() { return reported; }
        public void setReported(long reported) { this.reported = reported; }

        public long getRecovered() { return recovered; }
        public void setRecovered(long recovered) { this.recovered = recovered; }
    }

    public AdminStatsResponse() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getTotalLost() { return totalLost; }
    public void setTotalLost(long totalLost) { this.totalLost = totalLost; }

    public long getTotalFound() { return totalFound; }
    public void setTotalFound(long totalFound) { this.totalFound = totalFound; }

    public long getTotalRecovered() { return totalRecovered; }
    public void setTotalRecovered(long totalRecovered) { this.totalRecovered = totalRecovered; }

    public long getTotalMatches() { return totalMatches; }
    public void setTotalMatches(long totalMatches) { this.totalMatches = totalMatches; }

    public long getPendingClaims() { return pendingClaims; }
    public void setPendingClaims(long pendingClaims) { this.pendingClaims = pendingClaims; }

    public double getRecoveryRatePercentage() { return recoveryRatePercentage; }
    public void setRecoveryRatePercentage(double recoveryRatePercentage) { this.recoveryRatePercentage = recoveryRatePercentage; }

    public Map<String, Long> getCategoryDistribution() { return categoryDistribution; }
    public void setCategoryDistribution(Map<String, Long> categoryDistribution) { this.categoryDistribution = categoryDistribution; }

    public List<MonthlyTrend> getMonthlyTrends() { return monthlyTrends; }
    public void setMonthlyTrends(List<MonthlyTrend> monthlyTrends) { this.monthlyTrends = monthlyTrends; }
}

