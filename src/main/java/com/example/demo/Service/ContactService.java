package com.example.demo.Service;

import com.example.demo.Entity.Contact;
import com.example.demo.Entity.LinkPrecedence;
import com.example.demo.Repository.ContactRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

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
                .findFirst()
                .orElse(matchingContacts.get(0));

        // If the primary contact does not have the current email or phone number, create a new secondary contact
        Optional<Contact> existingContactOpt = matchingContacts.stream()
                .filter(contact -> email.equals(contact.getEmail()) && phoneNumber.equals(contact.getPhoneNumber()))
                .findFirst();

        if (existingContactOpt.isPresent()) {
            // The contact already exists, no need to create a new one
            return existingContactOpt.get();
        }

        Contact newSecondaryContact = new Contact();
        newSecondaryContact.setEmail(email);
        newSecondaryContact.setPhoneNumber(phoneNumber);
        newSecondaryContact.setLinkPrecedence(LinkPrecedence.SECONDARY);
        newSecondaryContact.setLinkedId(primaryContact.getId());
        newSecondaryContact.setCreatedAt(LocalDateTime.now());
        newSecondaryContact.setUpdatedAt(LocalDateTime.now());
        return contactRepository.save(newSecondaryContact);
    }

    public Map<String, Object> getConsolidatedContact(String email, String phoneNumber) {
        List<Contact> matchingContacts = contactRepository.findByEmailOrPhoneNumber(email, phoneNumber);

        if (matchingContacts.isEmpty()) {
            return null;
        }

        // Identify the primary contact among the matching contacts
        Contact primaryContact = matchingContacts.stream()
                .filter(contact -> contact.getLinkPrecedence() == LinkPrecedence.PRIMARY)
                .findFirst()
                .orElse(matchingContacts.get(0));

        Set<String> emails = new LinkedHashSet<>();
        Set<String> phoneNumbers = new LinkedHashSet<>();
        List<Integer> secondaryContactIds = new ArrayList<>();

        for (Contact contact : matchingContacts) {
            if (contact.getLinkPrecedence() == LinkPrecedence.PRIMARY) {
                emails.add(contact.getEmail());
                phoneNumbers.add(contact.getPhoneNumber());
            } else {
                secondaryContactIds.add(contact.getId());
                if (contact.getEmail() != null) {
                    emails.add(contact.getEmail());
                }
                if (contact.getPhoneNumber() != null) {
                    phoneNumbers.add(contact.getPhoneNumber());
                }
            }
        }

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
