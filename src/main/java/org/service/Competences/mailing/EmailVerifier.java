package org.service.Competences.mailing;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

public class EmailVerifier {

    public static boolean hasMxRecord(String email) {
        if (email == null) return false;

        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) return false;

        String domain = email.substring(at + 1).trim();
        if (domain.isEmpty()) return false;

        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("com.sun.jndi.dns.timeout.initial", "3000");
            env.put("com.sun.jndi.dns.timeout.retries", "1");

            DirContext ctx = new InitialDirContext(env);
            Attribute mx = ctx.getAttributes(domain, new String[]{"MX"}).get("MX");
            if (mx == null) return false;

            NamingEnumeration<?> all = mx.getAll();
            return all.hasMore();
        } catch (Exception e) {
            return false;
        }
    }
}
