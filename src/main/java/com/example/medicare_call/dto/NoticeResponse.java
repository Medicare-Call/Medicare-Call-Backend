package com.example.medicare_call.dto;

import com.example.medicare_call.domain.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeResponse {
    
    private Long id;
    private String title;
    private String author;
    private String contents;
    private String publishedAt;
    
    public static NoticeResponse from(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .author(notice.getAuthor())
                .contents(notice.getContents())
                .publishedAt(notice.getPublishedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build();
    }
} 