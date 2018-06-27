# BrightTalkBank

A small banking app that allows the user (jacquesja) or (johndoe) to perform banking transactions. 
It is built using Play, akka and scala

## Installation and Running instructions in command promt

* Clone repository
* Run sbt in project direcotry (where build.sbt located)
* Make sure sbt and java are on your path
* set SBT_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 (if you want to debug in Intelij) 
* Once sbt console, type run
* Navigate to localhost:9000 in a browser or using postman (this will compile using Play so might take a while)

## Using BrightTalkBank API

* There are 4 actions we can take using the api for 2 users jacquesja or johndoe
1. Deposit Money - This is a post so best to use postman
  localhost:9000/deposit 
  {
	  "clientId" : "jacquesja",
	  "amount" : 1000	
  }

2. Withdraw Money - This is a post so best to use postman
  localhost:9000/withdraw 
  {
	  "clientId" : "jacquesja",
	  "amount" : 50
  }
3. Get latest transactions
    localhost:9000/listTransactions/{username}/lines/{nr} 
    eg. localhost:9000/listTransactions/jacquesja/lines/3 
4. Get Current Balance
    localhost:9000/currentBalance/{username}
    eg. localhost:9000/currentBalance/jacquesja
