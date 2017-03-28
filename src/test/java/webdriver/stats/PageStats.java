package webdriver.stats;

/**
 * Created by p.ordenko on 03.09.2014.
 */
public class PageStats {
    private double redirectTime = 0d;
    private double appCacheTime = 0d;
    private double dnsTime = 0d;
    private double tcpTime = 0d;
    private double requestTime = 0d;
    private double responseTime = 0d;
    private double processingTime = 0d;
    private double onLoadTime = 0d;
    private double totalTime = 0d;

    public PageStats() {}

    public double getRedirectTime() {
        return redirectTime;
    }

    public void setRedirectTime(double redirectTime) {
        this.redirectTime = redirectTime;
    }

    public double getAppCacheTime() {
        return appCacheTime;
    }

    public void setAppCacheTime(double appCacheTime) {
        this.appCacheTime = appCacheTime;
    }

    public double getDnsTime() {
        return dnsTime;
    }

    public void setDnsTime(double dnsTime) {
        this.dnsTime = dnsTime;
    }

    public double getTcpTime() {
        return tcpTime;
    }

    public void setTcpTime(double tcpTime) {
        this.tcpTime = tcpTime;
    }

    public double getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(double requestTime) {
        this.requestTime = requestTime;
    }

    public double getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(double responseTime) {
        this.responseTime = responseTime;
    }

    public double getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(double processingTime) {
        this.processingTime = processingTime;
    }

    public double getOnLoadTime() {
        return onLoadTime;
    }

    public void setOnLoadTime(double onLoadTime) {
        this.onLoadTime = onLoadTime;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }
}
