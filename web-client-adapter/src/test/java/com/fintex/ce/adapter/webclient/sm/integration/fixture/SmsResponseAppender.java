package com.fintex.ce.adapter.webclient.sm.integration.fixture;

import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;

import java.util.List;

/**
 * Fluent builder for SMS-style stub rows used in Security Master HTTP integration tests.
 *
 * @param <T>
 *          SMS payload type (e.g. {@code AssetAllocation}, {@code Fees})
 * @param <A>
 *          per-row appendix passed to {@link #append}
 */
public interface SmsResponseAppender<T, A> {

  SmsResponseAppender<T, A> append(String id, FiIdentifierType idType, A appendix);

  List<SecurityAttributeResult<T>> build();
}
