package ru.gostmaster.common.data.crl;

import java.util.Date;

public interface Crl {
    String getCaSubject();
    String getCrlEncodedData();
    Date getNextUpdate();
    Date getThisUpdate();
    String getDownloadedFrom();
}
