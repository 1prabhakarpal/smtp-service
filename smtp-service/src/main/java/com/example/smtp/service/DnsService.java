package com.example.smtp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DnsService {

    public List<String> getMxRecords(String domain) {
        log.debug("Looking up MX records for domain: {}", domain);
        List<MxRecord> mxRecords = new ArrayList<>();
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext ictx = new InitialDirContext(env);

            Attributes attrs = ictx.getAttributes(domain, new String[] { "MX" });
            Attribute attr = attrs.get("MX");

            if (attr == null) {
                log.warn("No MX record found for domain: {}", domain);
                return Collections.emptyList();
            }

            for (int i = 0; i < attr.size(); i++) {
                String record = (String) attr.get(i);
                String[] parts = record.split("\\s+");
                if (parts.length == 2) {
                    int priority = Integer.parseInt(parts[0]);
                    String target = parts[1];
                    if (target.endsWith(".")) {
                        target = target.substring(0, target.length() - 1);
                    }
                    mxRecords.add(new MxRecord(priority, target));
                }
            }
        } catch (NamingException e) {
            log.error("DNS lookup failed for domain: {}", domain, e);
            return Collections.emptyList();
        }

        return mxRecords.stream()
                .sorted(Comparator.comparingInt(MxRecord::priority))
                .map(MxRecord::target)
                .collect(Collectors.toList());
    }

    private record MxRecord(int priority, String target) {
    }
}
