package com.bitespeed.assignment.service;

import com.bitespeed.assignment.dto.IdentifyRequest;
import com.bitespeed.assignment.dto.IdentifyResponse;
import com.bitespeed.assignment.model.Contact;
import com.bitespeed.assignment.repository.ContactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;

    @Transactional
    public IdentifyResponse identify(IdentifyRequest request) {
        // Validate input, must have at least phoneNumber or email
        if ((request.getEmail() == null || request.getEmail().isBlank()) &&
                (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank())) {
            throw new IllegalArgumentException("Either email or phoneNumber must be provided");
        }

        // Step 1: Find all contacts matching email or phoneNumber (soft deleted ignored - if needed)
        List<Contact> matchedContacts = contactRepository.findByEmailOrPhoneNumber(
                request.getEmail(), request.getPhoneNumber());

        if (matchedContacts.isEmpty()) {
            // No contacts found, create a new primary contact
            Contact newPrimary = Contact.builder()
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .linkPrecedence(Contact.LinkPrecedence.PRIMARY)
                    .linkedId(null)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();
            contactRepository.save(newPrimary);

            return buildResponse(newPrimary, Collections.emptyList());
        }

        // Step 2: Get all linked contacts grouped by primary
        Set<Integer> linkedIds = new HashSet<>();
        for (Contact c : matchedContacts) {
            if (c.getLinkPrecedence()== Contact.LinkPrecedence.PRIMARY) {
                linkedIds.add(c.getId());
            } else if (c.getLinkedId() != null) {
                linkedIds.add(c.getLinkedId());
            }
        }

        // Fetch all contacts linked to any primary contact found
        List<Contact> allLinkedContacts = new ArrayList<>();
        for (Integer primaryId : linkedIds) {
            List<Contact> linkedGroup = contactRepository.findByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber());
            // Actually, we need to find all contacts that point to primaryId or are that primaryId

            // Let's fetch all contacts where linkedId == primaryId or id == primaryId
            linkedGroup = contactRepository.findAll().stream()
                    .filter(ct -> ct.getId().equals(primaryId) || (ct.getLinkedId() != null && ct.getLinkedId().equals(primaryId)))
                    .toList();

            allLinkedContacts.addAll(linkedGroup);
        }

        // Step 3: Determine the oldest contact among allLinkedContacts to become primary
        Contact oldestPrimary = allLinkedContacts.stream()
                .filter(c -> c.getLinkPrecedence()== Contact.LinkPrecedence.PRIMARY)
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElseThrow(() -> new IllegalStateException("No primary contact found"));

        // Step 4: Check if new incoming info (email or phoneNumber) is not in contacts,
        // if so, create secondary contact linked to oldest primary
        boolean isEmailNew = request.getEmail() != null && allLinkedContacts.stream()
                .noneMatch(c -> request.getEmail().equals(c.getEmail()));

        boolean isPhoneNew = request.getPhoneNumber() != null && allLinkedContacts.stream()
                .noneMatch(c -> request.getPhoneNumber().equals(c.getPhoneNumber()));

        if (isEmailNew || isPhoneNew) {
            // Create secondary contact
            Contact newSecondary = Contact.builder()
                    .email(isEmailNew ? request.getEmail() : null)
                    .phoneNumber(isPhoneNew ? request.getPhoneNumber() : null)
                    .linkedId(oldestPrimary.getId())
                    .linkPrecedence(Contact.LinkPrecedence.SECONDARY)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

            contactRepository.save(newSecondary);

            allLinkedContacts.add(newSecondary);
        }

        // Step 5: If any primary contacts are younger than oldestPrimary, change them to secondary
        List<Contact> primariesToDemote = allLinkedContacts.stream()
                .filter(c -> c.getLinkPrecedence() == Contact.LinkPrecedence.PRIMARY)
                .filter(c -> !c.getId().equals(oldestPrimary.getId()))
                .toList();

        for (Contact primaryToDemote : primariesToDemote) {
            primaryToDemote.setLinkPrecedence(Contact.LinkPrecedence.SECONDARY);
            primaryToDemote.setLinkedId(oldestPrimary.getId());
            primaryToDemote.setUpdatedAt(OffsetDateTime.now());
            contactRepository.save(primaryToDemote);
        }

        // Now gather emails, phoneNumbers, and secondary IDs
        List<String> emails = allLinkedContacts.stream()
                .sorted(Comparator.comparing(Contact::getCreatedAt))
                .map(Contact::getEmail)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> phoneNumbers = allLinkedContacts.stream()
                .sorted(Comparator.comparing(Contact::getCreatedAt))
                .map(Contact::getPhoneNumber)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Integer> secondaryContactIds = allLinkedContacts.stream()
                .filter(c -> c.getLinkPrecedence() == Contact.LinkPrecedence.SECONDARY)
                .map(Contact::getId)
                .collect(Collectors.toList());

        return IdentifyResponse.builder()
                .contact(IdentifyResponse.ContactINFO.builder()
                        .primaryContactId(oldestPrimary.getId())
                        .emails(emails)
                        .phoneNumbers(phoneNumbers)
                        .secondaryContactIds(secondaryContactIds)
                        .build())
                .build();
    }

    private IdentifyResponse buildResponse(Contact primary, List<Contact> secondaries) {
        List<String> emails = new ArrayList<>();
        List<String> phones = new ArrayList<>();
        List<Integer> secondaryContactIds = new ArrayList<>();
        emails.add(primary.getEmail());
        phones.add(primary.getPhoneNumber());
        if (secondaries != null) {
            for (Contact c : secondaries) {
                if (c.getEmail() != null && !emails.contains(c.getEmail())) {
                    emails.add(c.getEmail());
                }
                if (c.getPhoneNumber() != null && !phones.contains(c.getPhoneNumber())) {
                    phones.add(c.getPhoneNumber());
                }
                secondaryContactIds.add(c.getId());
            }
        }
        return IdentifyResponse.builder()
                .contact(IdentifyResponse.ContactINFO.builder()
                        .primaryContactId(primary.getId())
                        .emails(emails)
                        .phoneNumbers(phones)
                        .secondaryContactIds(secondaryContactIds)
                        .build())
                .build();
    }
}



