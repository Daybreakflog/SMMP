package com.property.service;

import com.property.entity.Upload;
import com.property.dto.request.UploadDTO;
import com.property.dto.response.UploadVO;

public interface UploadService {

    UploadVO create(UploadDTO dto);

    UploadVO getById(String id);
}
