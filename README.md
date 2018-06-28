# BrightTalkBank

A small banking app that allows the user (jacquesja) or (johndoe) to perform banking transactions. 
It is built using Play, akka and scala

## Installation and Running instructions in command promt

* Clone repository
* Run sbt compile in project direcotry (where build.sbt located) to make sure everything is working 
* Run just sbt to open sbt console
* Make sure sbt and java are on your path
* set SBT_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 (if you want to debug in Intelij) 
* Once in the sbt console, type run
* Navigate to localhost:9000 in a browser or using postman (this will compile using Play so might take a while)

## Using BrightTalkBank API

* There are 4 actions we can take using the api for 2 users jacquesja or johndoe
* In the project aslo find 4 pictures of postman as examples
* Deposit Money - This is a post so best to use postman <br />
  localhost:9000/deposit  <br />
  { <br />
	  "clientId" : "jacquesja", <br />
	  "amount" : 1000	 <br />
  } <br />

* Withdraw Money - This is a post so best to use postman <br />
  localhost:9000/withdraw  <br />
  { <br />
	  "clientId" : "jacquesja", <br />
	  "amount" : 50 <br />
  } <br />
* Get latest transactions <br />
    localhost:9000/listTransactions/{username}/lines/{nr} <br />
    eg. localhost:9000/listTransactions/jacquesja/lines/3  <br />
* Get Current Balance <br />
    localhost:9000/currentBalance/{username} <br />
    eg. localhost:9000/currentBalance/jacquesja

## The system and limitations

* I have used a mocked BankAccountDAO with a map as its backing store. The results are returned as futures to mimic long running transactions. Clearly the MockBankAccountDAO is no thread safe in of istelf but all interaction with it only happen through the actor sytem.
* Most obvious limitaion, you dont have to use the prepulated users, when you deposit money for the first time you create a user that can be worked with. (You should be creating a user correctly) 
* More Validation is needed for the inputs to make it more obvious where inpput was incorrect
* I could not get the  localhost:9000/listTransactions/jacquesja/lines/{nr} to ignore the {nr} and just bring back all the lines. That is something that must be worked on

## Where to from here

* I have enjoyed this exercise especially getting to try out Play.
* I would have liked to add a login module which would create a session for a user (I would have created a User actor with Login, Logout)
* Next A session actor would be populated. The session would timeout if the user does not interact with the system. This would end the session and the user would have to login again.
* I would not have used the username of the user in my rest calls but the session token created when logging in and cross check it against the session
* The system requires more unit and integration testing, espacially the end points, but I ran out of time for the exercise to do that
