package api.util;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.*;

public class ExceptionCodeUtil {

  public static boolean isFromListOfReqValidationExceptionCodes(List<ExceptionCode> exceptionCodes) {
    final List<ExceptionCode> reqValidationExceptionCodes = List.of(ERR_RRC_CPED_001, ERR_RRC_CIPSD_001,
        ERR_RRC_CIPSD_002,
        ERR_RRC_CPSD_001, ERR_RRC_CPSD_004, ERR_RRC_MC_001, ERR_BWP_BWPTIP_001,
        ERR_BWP_BWPTIP_002, ERR_RRC_RTIP_001, ERR_RRC_CNOB_001, ERR_RRC_CNOB_002, ERR_RRC_RTIP_003, ERR_RRC_TIP_001,
        ERR_RRC_TIP_002, ERR_RRC_TIP_003, ERR_RRC_TIP_004, ERR_RRC_TIP_005, ERR_RRC_TIP_006, ERR_RRC_TIP_007,
        ERR_RRC_TIP_008);
    return exceptionCodes.stream().filter(Objects::nonNull).anyMatch(reqValidationExceptionCodes::contains);
  }
}
