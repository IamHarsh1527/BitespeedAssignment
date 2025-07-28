package com.bitespeed.assignment.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class IdentifyResponse {
    private ContactINFO contact;

    @Data
    @Builder
    public static class ContactINFO {
        private Integer primaryContactId;
        private List<String> emails;
        private List<String> phoneNumbers;
        private List<Integer> secondaryContactIds;
    }
}


