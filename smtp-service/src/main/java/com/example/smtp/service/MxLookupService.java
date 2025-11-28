package com.example.smtp.service;

import org.springframework.stereotype.Service;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;
import org.xbill.DNS.TextParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class MxLookupService {

    public List<String> getMxRecords(String domain) {
        try {
            Record[] records = new Lookup(domain, Type.MX).run();
            if (records == null || records.length == 0) {
                return Collections.emptyList();
            }

            List<MXRecord> mxRecords = new ArrayList<>();
            for (Record record : records) {
                if (record instanceof MXRecord) {
                    mxRecords.add((MXRecord) record);
                }
            }

            mxRecords.sort(Comparator.comparingInt(MXRecord::getPriority));

            List<String> sortedHosts = new ArrayList<>();
            for (MXRecord mxRecord : mxRecords) {
                String target = mxRecord.getTarget().toString();
                if (target.endsWith(".")) {
                    target = target.substring(0, target.length() - 1);
                }
                sortedHosts.add(target);
            }
            return sortedHosts;

        } catch (TextParseException e) {
            return Collections.emptyList();
        }
    }
}
