package com.fintex.ce.model.redis.core;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.exception.DataErrorException;
import com.fintex.ce.config.RedisConfig;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.redis.core.index.Indexed;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class RedisId {

    @Id
    private String id;

    @Indexed
    private String holdingId;

    @Indexed
    private String prefixEnv = RedisConfig.PREFIX_ENV;

    // user requested providers
    @Indexed
    private String providers;
    @Indexed
    private String provider;

    @Transient
    private List<DataErrorException> errors = new ArrayList<>();

    public RedisId() {
        // keep provider as "" by default
        provider = "";
        providers = "";
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void addError(DataErrorException error) {
        this.errors.add(error);
    }

    public boolean hasMonthlyReturnsErrors() {
        return errors.stream().anyMatch(e ->
                e.getCode().equals(ExceptionCode.ERR_RRC_MMR_001) || e.getCode().equals(ExceptionCode.ERR_RRC_MMR_002));
    }

    public List<DataErrorException> getOnlyMonthlyReturnsErrors() {
        return errors.stream()
                .filter(e -> e.getCode().equals(ExceptionCode.ERR_RRC_MMR_001) || e.getCode().equals(ExceptionCode.ERR_RRC_MMR_002))
                .toList();
    }
}
