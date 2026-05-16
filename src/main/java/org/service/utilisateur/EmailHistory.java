package org.service.utilisateur;

import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

public final class EmailHistory {

    private static final String PREF_NODE = "skillswap";
    private static final String KEY_EMAILS = "email_history";
    private static final String SEP = ";;";
    private static final int MAX = 8;

    private static final Preferences prefs = Preferences.userRoot().node(PREF_NODE);

    private EmailHistory() {}

    public static List<String> getAll() {
        String raw = prefs.get(KEY_EMAILS, "");
        if (raw == null || raw.isBlank()) return new ArrayList<>();

        String[] parts = raw.split(Pattern.quote(SEP));
        LinkedHashSet<String> set = new LinkedHashSet<>();

        for (String p : parts) {
            String e = (p == null) ? "" : p.trim();
            if (!e.isEmpty()) set.add(e);
        }
        return new ArrayList<>(set);
    }

    public static void add(String email) {
        if (email == null) return;
        String e = email.trim();
        if (e.isEmpty()) return;

        List<String> list = getAll();
        list.removeIf(x -> x.equalsIgnoreCase(e));
        list.add(0, e);

        if (list.size() > MAX) list = list.subList(0, MAX);

        save(list);
    }

    public static void remove(String email) {
        if (email == null) return;
        String e = email.trim();
        if (e.isEmpty()) return;

        List<String> list = getAll();
        list.removeIf(x -> x.equalsIgnoreCase(e));
        save(list);
    }

    public static void clear() {
        prefs.remove(KEY_EMAILS);
    }

    private static void save(List<String> emails) {
        String raw = String.join(SEP, emails);
        prefs.put(KEY_EMAILS, raw);
    }
}