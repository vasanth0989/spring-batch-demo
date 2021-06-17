package com.test.checkdeposit;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckDeposit {

    private String customerNumber;
    private String checkNumber;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String firstName;
    private String lastName;
    private String bankName;
    private String amount;
}
