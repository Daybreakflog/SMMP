package com.property.service.impl;

import com.property.service.UploadService;

import cn.dev33.satoken.stp.StpUtil;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.Upload;
import com.property.mapper.UploadMapper;
import com.property.dto.request.UploadDTO;
import com.property.dto.response.UploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final UploadMapper uploadMapper;

    @Transactional
    public UploadVO create(UploadDTO dto) {
        Upload upload = new Upload();
        upload.setName(dto.getName());
        upload.setSize(dto.getSize());
        upload.setMimeType(dto.getMimeType());
        upload.setUrl(dto.getUrl());
        upload.setOssKey(dto.getOssKey());
        upload.setCreatorId(StpUtil.getLoginIdAsString());
        uploadMapper.insert(upload);
        return toVO(upload);
    }

    public UploadVO getById(String id) {
        Upload upload = uploadMapper.selectById(id);
        if (upload == null) throw new BusinessException(ErrorCode.UPLOAD_NOT_FOUND);
        return toVO(upload);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private UploadVO toVO(Upload u) {
        UploadVO vo = new UploadVO();
        vo.setId(u.getId());
        vo.setName(u.getName());
        vo.setSize(u.getSize());
        vo.setMimeType(u.getMimeType());
        vo.setUrl(u.getUrl());
        vo.setOssKey(u.getOssKey());
        vo.setCreatorId(u.getCreatorId());
        vo.setCreatedAt(u.getCreatedAt());
        return vo;
    }
}
