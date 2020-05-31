package ru.gostmaster.verification.impl;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import ru.gostmaster.verification.Check;
import ru.gostmaster.verification.VerificationChecksService;

import java.util.List;

/**
 * Осуществляем шаги проверки при помощи  bouncy castle.
 * 
 * @author maksimgurin 
 */
@Slf4j
public class VerificationChecksServiceIImpl implements VerificationChecksService {
    private List<Check> checks;

    public VerificationChecksServiceIImpl(List<Check> checks) {
        this.checks = ImmutableList.copyOf(checks);
    }

    public List<Check> getChecks() {
        return checks;
    }
}
