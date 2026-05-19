package com.property.service.impl;

import com.property.service.TenantService;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.Tenant;
import com.property.mapper.TenantMapper;
import com.property.dto.request.TenantDTO;
import com.property.dto.request.TenantQueryDTO;
import com.property.dto.request.TenantImportRow;
import com.property.dto.response.ImportPreviewVO;
import com.property.dto.response.TenantVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantMapper tenantMapper;

    public TenantVO create(TenantDTO dto) {
        Tenant tenant = new Tenant();
        applyDTO(tenant, dto);
        tenant.setStatus("ACTIVE");
        tenantMapper.insert(tenant);
        return toVO(tenant);
    }

    public TenantVO getById(String id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ErrorCode.TENANT_NOT_FOUND);
        }
        return toVO(tenant);
    }

    public TenantVO update(String id, TenantDTO dto) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ErrorCode.TENANT_NOT_FOUND);
        }
        applyDTO(tenant, dto);
        tenantMapper.updateById(tenant);
        return toVO(tenant);
    }

    public void delete(String id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ErrorCode.TENANT_NOT_FOUND);
        }
        tenantMapper.deleteById(id);
    }

    public PageResult<TenantVO> page(TenantQueryDTO query) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<Tenant>()
                .and(StrUtil.isNotBlank(query.getKeyword()), w ->
                        w.like(Tenant::getName, query.getKeyword())
                         .or().like(Tenant::getPhone, query.getKeyword()))
                .eq(StrUtil.isNotBlank(query.getType()), Tenant::getType, query.getType())
                .orderByDesc(Tenant::getCreatedAt);

        return PageResult.of(tenantMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper
        ).convert(this::toVO));
    }

    public List<Object> getContracts(String id) {
        if (tenantMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.TENANT_NOT_FOUND);
        }
        return List.of();
    }

    public List<Object> getWorkOrders(String id) {
        if (tenantMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.TENANT_NOT_FOUND);
        }
        return List.of();
    }

    public ImportPreviewVO importPreview(MultipartFile file) throws IOException {
        return processImport(file, false);
    }

    public ImportPreviewVO importCommit(MultipartFile file) throws IOException {
        return processImport(file, true);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private ImportPreviewVO processImport(MultipartFile file, boolean persist) throws IOException {
        List<TenantImportRow> rows = EasyExcel.read(file.getInputStream())
                .head(TenantImportRow.class)
                .sheet()
                .doReadSync();

        List<ImportPreviewVO.RowError> errors = new ArrayList<>();
        int success = 0;

        for (int i = 0; i < rows.size(); i++) {
            TenantImportRow row = rows.get(i);
            int rowNum = i + 2; // header is row 1

            List<String> rowErrors = validateImportRow(row);
            if (!rowErrors.isEmpty()) {
                errors.add(new ImportPreviewVO.RowError(rowNum, String.join("; ", rowErrors)));
                continue;
            }

            if (persist) {
                try {
                    Tenant tenant = fromImportRow(row);
                    tenantMapper.insert(tenant);
                    success++;
                } catch (Exception e) {
                    errors.add(new ImportPreviewVO.RowError(rowNum, "保存失败：" + e.getMessage()));
                }
            } else {
                success++;
            }
        }

        ImportPreviewVO vo = new ImportPreviewVO();
        vo.setSuccessRows(success);
        vo.setFailRows(errors.size());
        vo.setErrors(errors);
        return vo;
    }

    private List<String> validateImportRow(TenantImportRow row) {
        List<String> errors = new ArrayList<>();

        String type = normalizeType(row.getType());
        if (type == null) {
            errors.add("类型无效，只能填 PERSONAL/个人 或 COMPANY/企业");
        }
        if (StrUtil.isBlank(row.getName())) {
            errors.add("姓名/公司名称不能为空");
        }
        if (StrUtil.isBlank(row.getPhone())) {
            errors.add("手机号不能为空");
        } else if (!row.getPhone().matches("^\\d{11}$")) {
            errors.add("手机号必须为11位数字");
        }
        if (StrUtil.isNotBlank(row.getIdCard()) && !row.getIdCard().matches("^\\d{17}[\\dXx]$")) {
            errors.add("身份证号必须为18位");
        }
        if (StrUtil.isNotBlank(row.getSocialCreditCode())
                && !row.getSocialCreditCode().matches("^[0-9A-HJ-NP-RT-UW-Y]{18}$")) {
            errors.add("统一社会信用代码必须为18位");
        }
        return errors;
    }

    private Tenant fromImportRow(TenantImportRow row) {
        Tenant t = new Tenant();
        t.setType(normalizeType(row.getType()));
        t.setName(row.getName());
        t.setPhone(row.getPhone());
        t.setIdCard(StrUtil.isBlank(row.getIdCard()) ? null : row.getIdCard());
        t.setSocialCreditCode(StrUtil.isBlank(row.getSocialCreditCode()) ? null : row.getSocialCreditCode());
        t.setContactName(row.getContactName());
        t.setContactPhone(row.getContactPhone());
        t.setBankAccount(row.getBankAccount());
        t.setStatus("ACTIVE");
        return t;
    }

    private String normalizeType(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if ("PERSONAL".equalsIgnoreCase(s) || "个人".equals(s)) return "PERSONAL";
        if ("COMPANY".equalsIgnoreCase(s) || "企业".equals(s)) return "COMPANY";
        return null;
    }

    private void applyDTO(Tenant tenant, TenantDTO dto) {
        if (dto.getType() != null) tenant.setType(dto.getType());
        if (dto.getName() != null) tenant.setName(dto.getName());
        if (dto.getPhone() != null) tenant.setPhone(dto.getPhone());
        if (dto.getIdCard() != null) tenant.setIdCard(dto.getIdCard());
        if (dto.getSocialCreditCode() != null) tenant.setSocialCreditCode(dto.getSocialCreditCode());
        if (dto.getContactName() != null) tenant.setContactName(dto.getContactName());
        if (dto.getContactPhone() != null) tenant.setContactPhone(dto.getContactPhone());
        if (dto.getBankAccount() != null) tenant.setBankAccount(dto.getBankAccount());
    }

    private TenantVO toVO(Tenant t) {
        TenantVO vo = new TenantVO();
        vo.setId(t.getId());
        vo.setType(t.getType());
        vo.setName(t.getName());
        vo.setPhone(t.getPhone());
        vo.setIdCard(t.getIdCard());
        vo.setSocialCreditCode(t.getSocialCreditCode());
        vo.setContactName(t.getContactName());
        vo.setContactPhone(t.getContactPhone());
        vo.setBankAccount(t.getBankAccount());
        vo.setStatus(t.getStatus());
        vo.setCreatedAt(t.getCreatedAt());
        vo.setUpdatedAt(t.getUpdatedAt());
        return vo;
    }
}
