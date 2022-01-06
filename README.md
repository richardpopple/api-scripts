# API client scripts

# Introduction
This project provides Kotlin code to coordinate API calls.

As such, it is very useful for one-off tasks like migrations.

# Features
There is currently only 1 function, invoked from the Main class.

Get all the prisons from the prisonAPI
Get all the users who already have the dps role specified
For each prison got all the users who have the nomis role for that prison
Give each of these users the dps role, excluding any who had it to begin with or have been given it as part of this process

The process can be rerun as we do not try to give the role to users whom already have it. 

The Main class defines all the configuration and wires up the dependencies.

There is 1 output:
* The console, which gives messages indicating what is happening and lists the users whom have 
