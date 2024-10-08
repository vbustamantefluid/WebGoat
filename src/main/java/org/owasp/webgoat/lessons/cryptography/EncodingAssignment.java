/*
 * This file is part of WebGoat, an Open Web Application Security Project utility. For details, please see http://www.owasp.org/
 *
 * Copyright (c) 2002 - 2019 Bruce Mayhew
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * Getting Source ==============
 *
 * Source for this application is maintained at https://github.com/WebGoat/WebGoat, a repository for free software projects.
 */

package org.owasp.webgoat.lessons.cryptography;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Random;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;





@RestController
public class EncodingAssignment extends AssignmentEndpoint {

  public static String getBasicAuth(String username, String password) {
    return Base64.getEncoder().encodeToString(username.concat(":").concat(password).getBytes());
  }

  

@GetMapping(path = "/crypto/encoding/basic", produces = MediaType.TEXT_HTML_VALUE)
@ResponseBody
public String getBasicAuth(HttpServletRequest request) {

    String basicAuth = (String) request.getSession().getAttribute("basicAuth");
    String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;

    if (basicAuth == null) {
        // Validate the username to ensure it is valid and non-malicious
        if (username == null || !isValidUsername(username)) {
            // Handle invalid username scenario
            return "Invalid user";
        }

        String password = HashingAssignment.SECRETS[new SecureRandom().nextInt(HashingAssignment.SECRETS.length)];
        basicAuth = getBasicAuth(username, password);

        // Validate the generated basicAuth string
        if (isValidBasicAuth(basicAuth)) {
            request.getSession().setAttribute("basicAuth", basicAuth);
        } else {
            // Handle invalid basicAuth scenario
            return "Invalid authorization token";
        }
    }
    
    return "Authorization: Basic ".concat(basicAuth);
}

private boolean isValidUsername(String username) {
    // Example validation: username must be alphanumeric and between 3 and 20 characters
    Pattern pattern = Pattern.compile("^[a-zA-Z0-9]{3,20}$");
    Matcher matcher = pattern.matcher(username);
    return matcher.matches();
}

private boolean isValidBasicAuth(String basicAuth) {
    // Example validation: basicAuth must be a valid Base64 encoded string
    try {
        byte[] decodedBytes = Base64.getDecoder().decode(basicAuth);
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
        // Check if the decoded string follows the pattern of "username:password"
        return decodedString.contains(":");
    } catch (IllegalArgumentException e) {
        // If decoding fails, it's not a valid Base64 string
        return false;
    }
}

private String getBasicAuth(String username, String password) {
    // Combine username and password with a colon
    String authString = username + ":" + password;
    // Encode the combined string using Base64
    return Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
}


// Basic validation for username
private boolean isValidUsername(String username) {
    // Define a regular expression for allowed username patterns
    String regex = "^[a-zA-Z0-9._-]{3,}$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(username);
    return matcher.matches();
}

// Basic validation for the basic authentication header
private boolean isValidBasicAuth(String basicAuth) {
    // Basic validation: Check if the basicAuth is Base64 encoded
    try {
        Base64.getDecoder().decode(basicAuth);
        return true;
    } catch (IllegalArgumentException e) {
        return false;
    }
}


  @PostMapping("/crypto/encoding/basic-auth")
  @ResponseBody
  public AttackResult completed(
      HttpServletRequest request,
      @RequestParam String answer_user,
      @RequestParam String answer_pwd) {
    String basicAuth = (String) request.getSession().getAttribute("basicAuth");
    if (basicAuth != null
        && answer_user != null
        && answer_pwd != null
        && basicAuth.equals(getBasicAuth(answer_user, answer_pwd))) {
      return success(this).feedback("crypto-encoding.success").build();
    } else {
      return failed(this).feedback("crypto-encoding.empty").build();
    }
  }
}
