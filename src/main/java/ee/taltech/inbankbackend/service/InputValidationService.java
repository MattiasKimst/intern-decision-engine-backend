package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeParser;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.InvalidAgeException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;

import java.time.Period;

public class InputValidationService {

    // Used to check for the validity of the presented ID code.
    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();

    //ID code parsers used for extracting age from code
    private final EstonianPersonalCodeParser parserEstonia = new EstonianPersonalCodeParser();


    /**
     * Verify that all inputs are valid according to business rules.
     * If inputs are invalid, then throws corresponding exceptions.
     *
     * @param personalCode Provided personal ID code
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws InvalidAgeException If the person requesting loan is underage or older than maximum age eligible for loan
     */
    protected void verifyInputs(String personalCode, Long loanAmount, int loanPeriod) throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException, PersonalCodeException, InvalidAgeException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_AMOUNT <= loanAmount)
                || !(loanAmount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_PERIOD <= loanPeriod)
                || !(loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD)) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }

        /*
        Verify the age of provided ID - for simplicity we say that ID code and expected lifetime is same for
        all baltic states, we use Estonian ID code parser to extract the age (in reality works for Lithuanian code too,
        as it is exactly in the same format, but not for Latvian)
         */

        Period age = parserEstonia.getAge(personalCode);

        if (age.getYears() < DecisionEngineConstants.MINIMUM_AGE) {
            throw new InvalidAgeException("Age below minimum age eligible for loan");
        }
        //maximum eligible age for loan is expected lifetime - maximum loan period in years
        int maximumAgeEligible = DecisionEngineConstants.MAXIMUM_AGE - (int) Math.ceil((double) DecisionEngineConstants.MAXIMUM_LOAN_PERIOD / 12);

        if (age.getYears() > maximumAgeEligible) {
            throw new InvalidAgeException("Age above maximum age eligible for loan");
        }

    }
}
