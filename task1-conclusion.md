# Conclusion ticket-101

### "highlight what the intern did well"
- <b>Exception handling</b> What I see is well done is exception handling, different specific exceptions with specific messages have been created 
for different scenarios like invalidPersonalCodeException.
This way is easy to determine invalid input values and types. 
- <b>Readability and documentation </b>The code is readable, variable and method names are meaningful, comments are mostly sufficient to understand code. 
- <b>Encapsulation </b> Encapsulation is well done, e.g having separate methods for logic 
regarding the loan calculation,  input validation, credit modifier calculation, and loan amount  determination (however, I will describe later what's still wrong with them)
- <b>Tests</b> A good thing is that junit tests cover the logic of DecisionEngine for main cases (should consider covering more edge and negative cases). Also, DecisionEngineController is covered with integration tests.


### "as well as places for improvement."

1. <b>Unnecessary complexity in verifyInputs method</b>  :
    ```java
    if (!(DecisionEngineConstants.MINIMUM_LOAN_AMOUNT <= loanAmount)
                    || !(loanAmount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)) {
                throw new InvalidLoanAmountException("Invalid loan amount!");
            }
    ```
    Using ! here adds unnecessary complexity while trying to understand or maintain it, would be 
    better to have the if condition refactored like 
    ```java
    (loanAmount < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT) || (loanAmount > DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)
     ```
    Also, it would be beneficial to split it into two different if clauses and set a more specific error message saying if requested amount was below minimum or above maximum.
    The same applies to checking that period is valid.


2.  <b>highestValidLoanAmount logic and it's documentation</b> 

    The logic behind calculating maximum amount in highestValidLoanAmount was not clear at first glance and is different from intuitive approach based on ticket, it would have been good
    to have clearly stated in comments or by the way the calculation is done that it finds the maximum amount which could be given as loan with defined period and still having credit score >1
    e.g its calculation logic is derived from formula 
     ```
    credit score = (credit modifier / loan amount) * loan period =>
     1 = (credit modifier / loan amount) * loan period =>
     loan amount=credit modifier * loan period
     ```
    so any loan amount smaller than that results in score >1 and is thus approved, loan amount bigger than calculated amount results in score <1 and is rejected. In summary, implementation is good, documentation is not.



3. <b>For loop trying to find suitable loan period</b> 

    The lines 54-55
    ```java
    while (highestValidLoanAmount(loanPeriod) < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT) {
    loanPeriod++;
    ```
    The first for loop increases loan period until the highest acceptable loan amount is at least as big as defined in business rules as minimum loan amount.
    It is important to note that if highestValidLoanAmount(loanPeriod) for some reason never exceeds minimum loan amount constant, this loop is infinite and results in application crashing.
    It is unnecessary to use a for loop here at all, we can simply calculate minimum suitable loan period by using formula 
    derived from credit score formula when score = 1, longer or equal period than this means score>=1 and is thus approved
   ```java
    loanPeriod=(int) Math.ceil((double) DecisionEngineConstants.MINIMUM_LOAN_AMOUNT / creditModifier)
    ```

4. <b>Class variables</b>

   CreditModifier is defined as a class variable, however, it is different for each separate decision and is not a class 
   variable by nature, there is a risk that if somehow we fail to calculate a new modifier value, the old incorrect value is 
   still in memory and we use it for new decision.
   Therefore, it is better to instantiate creditModifier for each decision separately with value 0 at the beginning.


5. <b>Project structure</b>

    Project structure does not follow best practices for Spring Boot projects. Decision class in service directory is not a 
    service but data model, should be in model's directory. The same goes with DecisionRequest and DecisionResponse, i'd move
    them to models directory and rename endpoint directory as controller.


## The biggest issue is with following single responsibility principle
- In general, <b>having one class for various logic</b> is not a good practice making project less maintainable. It is <b>violating single responsibility principle</b> in SOLID principles
which states that each different responsibility should be defined in different class, e.g input validation, credit modifier calculation, loan amount determination.
I refactored DecisionEngine by creating separate services for each responsibility. 
- Also, it is <b>worth considering refactoring
those classes as interfaces and their implementations</b> to follow open/closed principle.