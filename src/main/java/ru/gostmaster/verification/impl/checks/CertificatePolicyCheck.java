package ru.gostmaster.verification.impl.checks;

import com.google.common.collect.ImmutableSet;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInformation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.gostmaster.dictionary.CertificatePoliciesName;
import ru.gostmaster.verification.Check;
import ru.gostmaster.verification.data.CheckResult;
import ru.gostmaster.verification.data.CheckResults;

import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Проверка Certificate Policy и класса использования сертификата.
 * 
 * @author maksimgurin 
 */
@Component
public class CertificatePolicyCheck implements Check {
    
    private static final Set<String> KS1_POLICIES = ImmutableSet.<String>builder()
        .add("1.2.643.100.113.1")
        .build();

    private static final Set<String> KS2_POLICIES = ImmutableSet.<String>builder()
        .addAll(KS1_POLICIES)
        .add("1.2.643.100.113.2")
        .build();

    private static final Set<String> KS3_POLICIES = ImmutableSet.<String>builder()
        .addAll(KS2_POLICIES)
        .add("1.2.643.100.113.3")
        .build();
    
    private static final Set<String> KV1_POLICIES = ImmutableSet.<String>builder()
        .addAll(KS3_POLICIES)
        .add("1.2.643.100.113.4")
        .build();
    
    private static final Set<String> KV2_POLICIES = ImmutableSet.<String>builder()
        .addAll(KV1_POLICIES)
        .add("1.2.643.100.113.5")
        .build();

    private static final Set<String> KA1_POLICIES = ImmutableSet.<String>builder()
        .addAll(KV2_POLICIES)
        .add("1.2.643.100.113.6")
        .build();
    
    private static final String CORRECT_POLICY_TEMPLATE = "Политика сертификата подтверждена. Класс средства: [%s]";
    private static final String INCORRECT_POLICY_TEMPLATE = "Политика сертификата не подтверждена. " +
        "Не удалось подтвердить класс средства. Обратите внимание на раздел certificatePolicies";
    
    @Override
    public Mono<CheckResult> verify(SignerInformation signerInformation, X509CertificateHolder certificateHolder) {
        boolean res;
        String message;
            
        CertificatePolicies policies = CertificatePolicies.fromExtensions(certificateHolder.getExtensions());
        Set<String> oids = Stream.of(policies.getPolicyInformation())
            .map(PolicyInformation::getPolicyIdentifier)
            .filter(Objects::nonNull)
            .map(String::valueOf)
            .collect(Collectors.toSet());
        
        if (oids.contains(KA1_POLICIES)) {
            res = true;
            message = String.format(CORRECT_POLICY_TEMPLATE, CertificatePoliciesName
                .getPolicyName(CertificatePoliciesName.KA1_OID));
        } else if (oids.containsAll(KV2_POLICIES)) {
            res = true;
            message = String.format(CORRECT_POLICY_TEMPLATE, CertificatePoliciesName
                .getPolicyName(CertificatePoliciesName.KV2_OID));
        } else if (oids.containsAll(KV1_POLICIES)) {
            res = true;
            message = String.format(CORRECT_POLICY_TEMPLATE, CertificatePoliciesName
                .getPolicyName(CertificatePoliciesName.KV1_OID));
        } else if (oids.containsAll(KS3_POLICIES)) {
            res = true;
            message = String.format(CORRECT_POLICY_TEMPLATE, CertificatePoliciesName
                .getPolicyName(CertificatePoliciesName.KS3_OID));
        } else if (oids.containsAll(KS2_POLICIES)) {
            res = true;
            message = String.format(CORRECT_POLICY_TEMPLATE, CertificatePoliciesName
                .getPolicyName(CertificatePoliciesName.KS2_OID));
        } else if (oids.containsAll(KS1_POLICIES)) {
            res = true;
            message = String.format(CORRECT_POLICY_TEMPLATE, CertificatePoliciesName
                .getPolicyName(CertificatePoliciesName.KS1_OID));
        } else {
            res = false;
            message = INCORRECT_POLICY_TEMPLATE;
        }
        
        CheckResult checkResult = new CheckResult();
        checkResult.setCode(CheckResults.CHECK_CERTIFICATE_POLICY);
        checkResult.setDescription(CheckResults.CHECK_CERTIFICATE_POLICY_DESCRIPTION);
        checkResult.setCreatedAt(new Date());
        checkResult.setSuccess(res);
        checkResult.setResultDescription(message);
        return Mono.just(checkResult);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
