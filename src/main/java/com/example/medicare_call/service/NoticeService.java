package com.example.medicare_call.service;

import com.example.medicare_call.domain.Notice;
import com.example.medicare_call.dto.NoticeResponse;
import com.example.medicare_call.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {
    
    private final NoticeRepository noticeRepository;

    public List<NoticeResponse> getAllNotices() {
        log.info("공지사항 목록 조회 시작");
        
        List<Notice> notices = noticeRepository.findAllByOrderByPublishedAtDesc();
        
        List<NoticeResponse> responses = notices.stream()
                .map(NoticeResponse::from)
                .collect(Collectors.toList());
        
        log.info("공지사항 목록 조회 완료: {}건", responses.size());
        
        return responses;
    }
} 