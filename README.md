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
