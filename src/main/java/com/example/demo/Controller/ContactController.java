package com.example.demo.Controller;

import com.example.demo.Entity.Contact;
import com.example.demo.Service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    @Autowired
    private ContactService contactService;

    @PostMapping("/addOrUpdate")
    public Contact addOrUpdateContact(@RequestParam(required = false) String email,
                                      @RequestParam(required = false) String phoneNumber) {
        return contactService.addOrUpdateContact(email, phoneNumber);
    }
    @PostMapping("/identify")
    public Map<String, Object> identifyContact(@RequestBody Map<String, Object> request) {
        String email = (String) request.get("email");
        String phoneNumber = (String) request.get("phoneNumber");
        return contactService.getConsolidatedContact(email, phoneNumber);
    }
}
