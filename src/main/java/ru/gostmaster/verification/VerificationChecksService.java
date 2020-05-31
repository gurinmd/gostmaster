package ru.gostmaster.verification;

import java.util.List;

/**
 * Класс для выполнения отдельных шагов верификации подписи.
 * 
 * @author maksimgurin 
 */
public interface VerificationChecksService {

    /**
     * Получить все необходимые проверки.
     * @return список объектов,которые будут проводить проверки
     */
    List<Check> getChecks();

}
