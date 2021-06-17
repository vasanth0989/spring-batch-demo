package com.test.checkdeposit.reader;

import com.test.checkdeposit.CheckDeposit;
import org.springframework.batch.item.file.LineMapper;

public class CheckDepositLineMapper implements LineMapper<CheckDeposit> {


    @Override
    public CheckDeposit mapLine(String line, int lineNumber) throws Exception {

        return null;
    }
}
