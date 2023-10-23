package com.picpaysimplificado.services;

import com.picpaysimplificado.DTO.TransactionDTO;
import com.picpaysimplificado.domain.transaction.Transaction;
import com.picpaysimplificado.domain.user.User;
import com.picpaysimplificado.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserTransaction {
 @Autowired
 private UserService userService;

 @Autowired
 private TransactionRepository repository;

 @Autowired
 private RestTemplate restTemplate;

 public void createTransaction(TransactionDTO transaction) throws Exception {
     User sender = this.userService.findUserById(transaction.senderId());
     User receiver = this.userService.findUserById(transaction.receiverId());

     userService.validateTransaction(sender, transaction.value());

     Boolean isAuthorized = this.authorizeTransaction(sender, transaction.value());
     if (!isAuthorized) {
         throw new Exception("Transação não autorizada!");
     }

     Transaction transaction1 = new Transaction();
     transaction1.setAmount(transaction.value());
     transaction1.setSender(sender);
     transaction1.setReceiver(receiver);
     transaction1.setTimeStamp(LocalDateTime.now());

     sender.setBalance(sender.getBalance().subtract(transaction.value()));
     receiver.setBalance(receiver.getBalance().add(transaction.value()));

     this.repository.save(transaction1);
     this.userService.saveUser(sender);
     this.userService.saveUser(receiver);
 }

 public Boolean authorizeTransaction(User sender, BigDecimal value) {
    ResponseEntity<Map> authorizationResponse = restTemplate.getForEntity("https://run.mocky.io/v3/8fafdd68-a090-496f-8c9a-3442cf30dae6", Map.class);

    if (authorizationResponse.getStatusCode() == HttpStatus.OK) {
        String message = (String) authorizationResponse.getBody().get("message");
        return "Autorizado".equalsIgnoreCase(message);
    } else return false;
 }
}
