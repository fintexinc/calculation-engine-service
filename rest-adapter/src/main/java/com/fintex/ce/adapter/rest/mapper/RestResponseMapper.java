package com.fintex.ce.adapter.rest.mapper;

import java.util.function.Supplier;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class RestResponseMapper {

    public <R, D> D toResponse(R result, Class<D> dtoClass) {
        try {
            D dto = dtoClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(result, dto);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map result to response", e);
        }
    }

    public <R, D> D toResponse(R result, Supplier<D> dtoSupplier) {
        D dto = dtoSupplier.get();
        BeanUtils.copyProperties(result, dto);
        return dto;
    }
}