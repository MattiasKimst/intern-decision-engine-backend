package ee.taltech.inbankbackend.service;

public class HighestLoanAmountCalculationService {

    /**
     * Calculates the largest valid loan for the current credit modifier and loan period.
     *
     * @return Largest valid loan amount
     */
    protected int highestValidLoanAmount(int loanPeriod, int creditModifier) {
        return creditModifier * loanPeriod;
    }

}
