# API client scripts

# Introduction
This project provides Kotlin code to coordinate API calls.

As such, it is very useful for one-off tasks like migrations.

# Features
There is currently only 1 function, invoked from Main.

It reads offender details form a spreadsheet (the format of which has been determined by another team).
These offender details are then used to recall then register them as a restricted patient.

The Main class defines all the configuration and wires up the dependencies.

There are 3 outputs:
* The console, which gives messages indicating what is happening
* A summary file called "SUMMARY-[date and time].txt". This provides a list of the outcomes of the migration for each offender.
* An output file for each offender. This gives information about how far the migration got and any error messages.