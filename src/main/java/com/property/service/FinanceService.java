package com.property.service;

import com.property.entity.*;
import com.property.mapper.*;
import com.property.dto.request.AdjustDTO;
import com.property.dto.request.ClaimRulesDTO;
import com.property.dto.request.CloseDTO;
import com.property.dto.request.ManualClaimDTO;
import com.property.dto.response.*;
import java.util.List;

public interface FinanceService {

    ReconciliationVO reconciliation(String period, String projectId);

    void adjust(AdjustDTO dto);

    CloseStatusVO closeStatus(String period, String projectId);

    CloseStatusVO close(CloseDTO dto, String operatorId);

    List<ClaimPoolVO> claimPool();

    void manualClaim(ManualClaimDTO dto);

    List<ClaimRuleVO> listRules(String projectId);

    List<ClaimRuleVO> saveRules(ClaimRulesDTO dto);
}
