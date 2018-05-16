package io.fundrequest.azrael.worker.contracts.transactions;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionRestController {

    private final TransactionService transactionService;

    public TransactionRestController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{transactionhash}")
    public TransactionStatus status(@PathVariable("transactionhash") final String transactionHash) {
        return transactionService.getStatus(transactionHash);
    }
}
