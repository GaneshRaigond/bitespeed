package com.example.demo.Service;

import com.example.demo.Entity.Contact;
import com.example.demo.Entity.LinkPrecedence;
import com.example.demo.Repository.ContactRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContactService {
    @Autowired
    private ContactRepository contactRepository;

    @Transactional
    public Contact addOrUpdateContact(String email, String phoneNumber) {
        List<Contact> matchingContacts = contactRepository.findByEmailOrPhoneNumber(email, phoneNumber);

        if (matchingContacts.isEmpty()) {
            // No matching contacts found, create a new primary contact
            Contact newContact = new Contact();
            newContact.setEmail(email);
            newContact.setPhoneNumber(phoneNumber);
            newContact.setLinkPrecedence(LinkPrecedence.PRIMARY);
            newContact.setCreatedAt(LocalDateTime.now());
            newContact.setUpdatedAt(LocalDateTime.now());
            return contactRepository.save(newContact);
        }

        // Identify the primary contact among the matching contacts
        Contact primaryContact = matchingContacts.stream()
                .filter(contact -> contact.getLinkPrecedence() == LinkPrecedence.PRIMARY)
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElse(matchingContacts.stream()
                        .min(Comparator.comparing(Contact::getCreatedAt))
                        .orElse(null));

        // If the primary contact does not have the current email or phone number, create a new secondary contact
        List<Contact> otherPrimaryContacts = matchingContacts.stream()
                .filter(contact -> contact.getLinkPrecedence() == LinkPrecedence.PRIMARY && !contact.equals(primaryContact))
                .collect(Collectors.toList());
        boolean emailExists = matchingContacts.stream().anyMatch(contact -> email.equals(contact.getEmail()));
        boolean phoneNumberExists = matchingContacts.stream().anyMatch(contact -> phoneNumber.equals(contact.getPhoneNumber()));

        if (!emailExists || !phoneNumberExists) {
            // Create a new secondary contact if the email or phone number is new
            Contact newSecondaryContact = new Contact();
            newSecondaryContact.setEmail(email);
            newSecondaryContact.setPhoneNumber(phoneNumber);
            newSecondaryContact.setLinkPrecedence(LinkPrecedence.SECONDARY);
            newSecondaryContact.setLinkedId(primaryContact.getId());
            newSecondaryContact.setCreatedAt(LocalDateTime.now());
            newSecondaryContact.setUpdatedAt(LocalDateTime.now());

            // Handle other primary contacts
            for (Contact otherPrimaryContact : otherPrimaryContacts) {
                otherPrimaryContact.setLinkPrecedence(LinkPrecedence.SECONDARY);
                otherPrimaryContact.setLinkedId(primaryContact.getId());
                otherPrimaryContact.setUpdatedAt(LocalDateTime.now());
                contactRepository.save(otherPrimaryContact);
            }

            contactRepository.save(newSecondaryContact);

        } else {
            if (otherPrimaryContacts.size() > 0) {
                for (Contact otherPrimaryContact : otherPrimaryContacts) {
                    otherPrimaryContact.setLinkPrecedence(LinkPrecedence.SECONDARY);
                    otherPrimaryContact.setLinkedId(primaryContact.getId());
                    otherPrimaryContact.setUpdatedAt(LocalDateTime.now());
                    contactRepository.save(otherPrimaryContact);
                }

                // Create a new secondary contact for the incoming request
                Contact newSecondaryContact = new Contact();
                newSecondaryContact.setEmail(email);
                newSecondaryContact.setPhoneNumber(phoneNumber);
                newSecondaryContact.setLinkPrecedence(LinkPrecedence.SECONDARY);
                newSecondaryContact.setLinkedId(primaryContact.getId());
                newSecondaryContact.setCreatedAt(LocalDateTime.now());
                newSecondaryContact.setUpdatedAt(LocalDateTime.now());
                contactRepository.save(newSecondaryContact);

            }

        }
        return primaryContact;
    }

    public Map<String, Object> getConsolidatedContact(String email, String phoneNumber) {
        List<Contact> matchingContacts = contactRepository.findByEmailOrPhoneNumber(email, phoneNumber);

        if (matchingContacts.isEmpty()) {
            Contact newContact = new Contact();
            newContact.setEmail(email);
            newContact.setPhoneNumber(phoneNumber);
            newContact.setLinkPrecedence(LinkPrecedence.PRIMARY);
            newContact.setCreatedAt(LocalDateTime.now());
            newContact.setUpdatedAt(LocalDateTime.now());
            contactRepository.save(newContact);

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> contactData = new HashMap<>();
            contactData.put("primaryContactId", newContact.getId());
            contactData.put("emails", Collections.singletonList(newContact.getEmail()));
            contactData.put("phoneNumbers", Collections.singletonList(newContact.getPhoneNumber()));
            contactData.put("secondaryContactIds", Collections.emptyList());
            response.put("contact", contactData);
            return response;
        }

        // Identify the primary contact among the matching contacts
        Contact primaryContact = matchingContacts.stream()
                .filter(contact -> contact.getLinkPrecedence() == LinkPrecedence.PRIMARY)
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElse(matchingContacts.stream()
                        .min(Comparator.comparing(Contact::getCreatedAt))
                        .orElse(null));

        List<Contact> otherPrimaryContacts = matchingContacts.stream()
                .filter(contact -> contact.getLinkPrecedence() == LinkPrecedence.PRIMARY && !contact.equals(primaryContact))
                .collect(Collectors.toList());

        boolean emailExists = matchingContacts.stream()
                .anyMatch(contact -> email != null && email.equals(contact.getEmail()));

        boolean phoneNumberExists = matchingContacts.stream()
                .anyMatch(contact -> phoneNumber != null && phoneNumber.equals(contact.getPhoneNumber()));

        if (!emailExists || !phoneNumberExists) {
            // Create a new secondary contact if the email or phone number is new
            Contact newSecondaryContact = new Contact();
            newSecondaryContact.setEmail(email);
            newSecondaryContact.setPhoneNumber(phoneNumber);
            newSecondaryContact.setLinkPrecedence(LinkPrecedence.SECONDARY);
            newSecondaryContact.setLinkedId(primaryContact.getId());
            newSecondaryContact.setCreatedAt(LocalDateTime.now());
            newSecondaryContact.setUpdatedAt(LocalDateTime.now());
            contactRepository.save(newSecondaryContact);

            // Handle other primary contacts
            for (Contact otherPrimaryContact : otherPrimaryContacts) {
                otherPrimaryContact.setLinkPrecedence(LinkPrecedence.SECONDARY);
                otherPrimaryContact.setLinkedId(primaryContact.getId());
                otherPrimaryContact.setUpdatedAt(LocalDateTime.now());
                contactRepository.save(otherPrimaryContact);
            }

            matchingContacts.add(newSecondaryContact);
        }

        List<String> emails = matchingContacts.stream()
                .map(Contact::getEmail)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> phoneNumbers = matchingContacts.stream()
                .map(Contact::getPhoneNumber)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Integer> secondaryContactIds = matchingContacts.stream()
                .filter(contact -> contact.getLinkPrecedence() == LinkPrecedence.SECONDARY)
                .map(Contact::getId)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> contactData = new HashMap<>();
        contactData.put("primaryContactId", primaryContact.getId());
        contactData.put("emails", new ArrayList<>(emails));
        contactData.put("phoneNumbers", new ArrayList<>(phoneNumbers));
        contactData.put("secondaryContactIds", secondaryContactIds);
        response.put("contact", contactData);

        return response;
    }

}
