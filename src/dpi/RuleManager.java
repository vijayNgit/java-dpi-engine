package dpi;

import java.util.*;

public class RuleManager {

    private Set<String> blockedIPs = new HashSet<>();
    private Set<AppType> blockedApps = new HashSet<>();
    private Set<String> blockedDomains = new HashSet<>();

    public void blockIP(String ip) { blockedIPs.add(ip); }
    public void blockApp(AppType app) { blockedApps.add(app); }
    public void blockDomain(String dom) { blockedDomains.add(dom); }

    public boolean isBlocked(String srcIP, AppType app, String sni) {
        if (blockedIPs.contains(srcIP)) return true;
        if (blockedApps.contains(app)) return true;
        if (sni != null)
            for (String d : blockedDomains)
                if (sni.contains(d)) return true;
        return false;
    }
}