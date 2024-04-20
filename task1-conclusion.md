### "highlight what the intern did well"
What I see is well done is exception handling, different specific exceptions with specific messages have been created 
for different scenarios like invalidPersonalCodeException.
This way is easy to determine invalid input values and types. The code is readable, 
variable and method names are meaningful, comments are mostly sufficient to understand code. Encapsulation is good, e.g having separate methods for logic 
regarding the loan calculation,  input validation, credit modifier calculation, and loan amount  determination.

### "as well as places for improvement."

In verifyInputs method :
```
if (!(DecisionEngineConstants.MINIMUM_LOAN_AMOUNT <= loanAmount)
                || !(loanAmount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
```
Using ! here adds unnecessary complexity, would be 
better to have the if condition refactored like 
```
(loanAmount < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT) || (loanAmount > DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)
 ```
Also, it would be beneficial to split it into two different if clauses and set a more specific error message saying if requested amount was below minimum or above maximum.
The same applies to checking that period is valid.

The logic behind calculating maximum amount in highestValidLoanAmount was not clear at first glance, it would have been good
to have clearly stated in comments or by the way the calculation is done that it finds the maximum amount which could be given as loan with defined period and still having credit score >1
e.g its calculation logic is derived from formula 
credit score = (credit modifier / loan amount) * loan period => 1 = (credit modifier / loan amount) * loan period => loan amount=credit modifier * loan period
so any loan amount smaller than that results in score >1 and is thus approved, loan amount bigger than calculated amount results in score <1 and is rejected

Currently, the lines 54-55
```
while (highestValidLoanAmount(loanPeriod) < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT) {
loanPeriod++;
```
The first for loop increases loan period until the highest acceptable loan amount is at least as big as defined in business rules as minimum loan amount.
It is unnecessary to use a for loop here, we can simply calculate it by using formula derived from credit score formula when score = 1, longer or equal period than this means score>=1 and is thus approved


CreditModifier is defined as a class variable, however, it is different for each separate decision, there is a risk that
if somehow we fail to calculate a new modifier value, the old incorrect value is still in memory and we use it for new decision.
Therefore, it is better to instantiate creditModifier for each decision separately with value 0 at the beginning.

#### The biggest issue is with following single responsibility principle
In general, having one class for various logic is not a good practice making project less maintainable. It is violating single responsibility principle in SOLID principles
which states that each different responsibility should be defined in different class, e.g input validation, credit modifier calculation, loan amount determination.
I refactored DecisionEngine by creating separate services for each responsibility.