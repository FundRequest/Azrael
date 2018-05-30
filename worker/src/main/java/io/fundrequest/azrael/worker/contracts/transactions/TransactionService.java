package io.fundrequest.azrael.worker.contracts.transactions;

import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;

@Service
public class TransactionService {

    private final Web3j web3j;

    public TransactionService(Web3j web3j) {
        this.web3j = web3j;
    }

    public TransactionStatus getStatus(final String transactionHash) {
        try {
            final EthGetTransactionReceipt send = web3j.ethGetTransactionReceipt(prettify(transactionHash)).send();
            return send.getTransactionReceipt()
                       .map(x -> (x.getStatus().equalsIgnoreCase("1") || x.getStatus().equalsIgnoreCase("0x1")) ? TransactionStatus.SUCCEEDED : TransactionStatus.FAILED)
                       .orElse(TransactionStatus.NOT_FOUND);
        } catch (final Exception ex) {
            return TransactionStatus.NOT_FOUND;
        }
    }

    private String prettify(final String address) {
        if (!address.startsWith("0x")) {
            return String.format("0x%s", address);
        } else {
            return address;
        }
    }
}
