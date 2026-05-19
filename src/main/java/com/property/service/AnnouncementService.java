package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.Announcement;
import com.property.dto.request.AnnouncementDTO;
import com.property.dto.request.AnnouncementQueryDTO;
import com.property.dto.response.AnnouncementVO;
import java.util.List;

public interface AnnouncementService {

    PageResult<AnnouncementVO> page(AnnouncementQueryDTO query);

    List<AnnouncementVO> active();

    AnnouncementVO create(AnnouncementDTO dto);

    AnnouncementVO getById(String id);

    AnnouncementVO patch(String id, AnnouncementDTO dto);

    AnnouncementVO publish(String id);

    AnnouncementVO revoke(String id);

    void delete(String id);
}
