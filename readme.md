# Play/Scala Web Application by LineDrop

## Overview

Concise Play Framework/Scala project to demonstrate the fundamentals of building a web application.  A complete 
subscriber app, it is designed to be a starting point for your own project by providing essential components of a
web application such as database integration, application configuration, logging, email functionality, user management, 
password hashing, and more.

You can use the project to quickly learn the basics and expand the existing codebase to add your own functionality.

### Topic covered

- Creating a project with SBT and importing it into IDE.
- Version control integration.
- Application structure, secret keys, and session cookies.
- User interface implementation with jQuery and Bootstrap.
- Application utilities: logging, database integration, password hashing, random generator, server environment, time and date formatting, email integration.
- Terminal commands: starting and stopping application and database.
- Securely initializing a super administrator.
- Creating and resetting passwords.
- Creating, editing, and authenticating users.
- Collecting and displaying subscriber data.
- Scheduling an automated system task.
- Error handling with user-friendly pages.
- Application maintenance: displaying version information and creating a sitemap.
- Library updates: keeping the language, framework, and library dependencies current.

## Requirements

Before running the project, please make sure you have the following installed:

- SBT _build and package management tool_: https://www.scala-sbt.org
- MongoDB Community Server: https://www.mongodb.com/download-center/community
- Compass Community _MongoDB GUI_: https://www.mongodb.com/download-center/compass
- IntelliJ IDEA Community Edition _integrated development environment_: https://www.jetbrains.com/idea
    - During the IntelliJ setup, install Scala plugin

### Importing Project

- Open IntelliJ
- Select Checkout from Version Control
- Paste URL: https://github.com/LineDrop/code.git
- Click Clone
- Click Yes to confirm
- Click Ok to accept default import settings
- Wait until the project is imported and the SBT has finished loading dependencies

### Configuring Application

- In IntelliJ, open conf/application.conf
- Update _Initialize Super Administrator_ section to allow for super administrator initialization
- Update _Gmail SMPT configuration_ section for the email to work,  please have the App Password ready for your gmail account
- Update additional configuration keys as necessary

### Running Project

#### Launch MongoDB Server
- In terminal, _cd_ into to the MongoDB folder
- Type `bin/mongod` to start database server

#### Launch Application
- In IntelliJ, click Terminal icon at the bottom of the window
- Type: `sbt run`
- Wait until you see _Listening for HTTP on..._

### Initialize Application
- Open a browser and go to http://localhost:9000
- Wait for the application to load
- Browse to http://localhost:9000/init/token, where _token_ is an alphanumeric string in the _sa.key_ configuration item

### Basic Application Test
- Check your email to set Super Administrator (SA) password
- Update SA password
- Sign in as SA
- Sign out
- Browse to http://localhost:9000 and enter your email to subscribe
- Sign in again as SA
- Notice the new subscriber

### Contact 

Contact us with comments and questions at code@linedrop.io.  