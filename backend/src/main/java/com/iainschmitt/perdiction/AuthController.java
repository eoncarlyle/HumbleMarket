package com.iainschmitt.perdiction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;


    // TODO
    // This needs to accept email, password, and secret code as JSON
    @PostMapping(value = "/signup")
    public ResponseEntity<String> getUserByUserName(@PathVariable String userName) {
        return ResponseEntity.ok(
            "Todo"
            //String.format("{'message': %s, 'userName': %s}")
        );
    }
}
