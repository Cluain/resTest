resTest - REST Testing Tool
=============

Tool to make REST APIs testing [especially performance testing] easier.

This work is licensed under LGPL.

Why?
-------

We were using REST testing tools like SoapUI, but found it insufficient for performance tests. We couldn't get real response times of our apis
and we couldn't stand issues like huge memory usage and constant hanging. Also we wanted to be able to easily run and modify test
cases on a remote system.

Description
-------

* Easily create test cases in plain XML
* Test case can have unlimited test steps [it's actually limited by your imagination and memory of your box]
* Step types: REST, Groovy, Loop, CSV [more to come]
* Easy logging configuration
* Report "real" response times
* Easy scripting


Goals
-----

* fast
* lightweight
* easy to script
* reporting real response times (from request to getting full response)
* easy to port test cases from other software
* easy to create and modify test cases without using GUI
* powerful logging


To do
-----

* check if json response type is working ok
* load testing
* Junit reporting
* configurable reports
* data sink step
* modify CSV step to allow more data source types
* refactor code not to look like nightmare


Configuration
-------------

### Scripts & data access


You can create groovy scripts for Groovy and REST steps. Every script will have three variables binded:
* log - SLF4J log variable
* testCase - whole test case object
* curent - current test step

#### Test Case

You can access following data:

* `testCase.testSteps` - map with test steps accessible by name, ie. `testCase.testSteps.someRestStep`
* `testCase.props` - map with properties accessible by name, ie. `testCase.props.someProperty`

You can also control flow of the test case using following methods:

* `testCase.runTestStep(String testStepName)` - will run this step and return back to caller
* `testCase.goToTestStep(String testStepName)` - will run this step next and proceed with steps following it

##### Example

Test case with steps:
1. stepA
2. stepB
3. stepC
4. stepD
5. stepE

StepB is a groovy step with following code:

``` groovy
    testCase.runTestStep("stepE")
    testCase.runTestStep("stepA")
    testCase.goToTestStep("stepD")
```

Now the test steps will be executed in this order:

1. stepA
2. stepB
3. stepE
4. stepA
5. stepD
6. stepE

as you can see, stepC was not executed.


#### Test Step

You can access following data:

* `current.params` - map of parameters accessible by name, ie. `current.params.someParamName`
* `current.props` - map of properties accessible by name, ie. `current.props.someProperty`
* `current.status` - HTTP status. Only for REST steps
* `current.response` - XML response. Only for REST steps.

Same data can be accessed from other steps using testCase, ie. `testCase.steps.someStepName.response`.

### Configuration details

#### Test Case

Top node of the xml configuration.

Case attributes:
* name - required. Name of the test case. Used in logging.
* url - optional. URL shared by the test steps. If URL for test steps varies it can be specified on test steps.

Case elements:
* property - optional. Name/value properties of the test case.
* sharedRestValidation - optional. Validation shared by all REST steps. For example you can check if every response status is equal 200.
* steps - described below.

#### REST

Primary step. Allows to prepare, send, validate and prorces request.
Response is automatically parsed to XML and stored in `response` variable. Response status is stored in `status` variable.

Step specific attributes:
* url - optional. Specify URL if different than URL on test case
* endpoint - required. REST service endpoint.
* method - optional. Specify HTTP method. Default is get.
* logResponse - optional, boolean. Specify whether response should be logged or not. Please see Logging section for more information.
* responseType - optional, xml or json. Type of response. If nothing is specified it will default to xml.

Step elements:
* beforeTest - optional, void. Script to run before sending request.
* validate - optional, boolean. Script to validate the response.
* afterTest - optional, void. Script to run after validation.

``` xml
    <rest name="restStepName2" url="http://myNewProject.com" endpoint="/something/login" method="get" responseType="xml">
        <beforeTest><![CDATA[ log.debug("beforeTest")
            log.info("${testCase.testSteps.size()}")
         ]]></beforeTest>
        <validate><![CDATA[ log.debug("validate") ]]></validate>
        <afterTest><![CDATA[ log.debug("afterTest")

                log.info("TransmissionEvent type: ${current.response.'@type'}")
        ]]></afterTest>
        <param name="id" value="${testCase.props.clientId}"/>
        <param name="dateOfBirth" value="${testCase.steps.restStepName1.response.client[id==3].@dateOfBirth}"
    </rest>
```

You can access response xml as described in [XMLParser](http://groovy.codehaus.org/Reading+XML+using+Groovy's+XmlParser).

#### Groovy

Execute groovy script.
This step can be used for data generation, flow control, whatever you can imagine and code, I suppose.
Just and idea, but you can also use this script to create a database connection, store a reference to the data source on test case and allow every other test step to access it.

``` xml
    <groovy name="groovyStepName">
        <![CDATA[ log.debug("we rock"); ]]>
    </groovy>
```

#### CSV

Read csv files and allow test steps to access their contents by assigning values to the properties of test step.
We use CSVReader to read the file, so it should cover everything. File is read line by line [one line per test step execution].
Values of the line are exposed under properties of the test step as defined in test step configuration.

Step specific attributes:
* fileName - required. Name/path of the file to read.
* separator - optional. Character separating entries in file.

Step specific elements:
* param - required at least one. Define names of the properties and index of csv file field.

For csv file `data.csv` with contents like:

```
    "12312333sdfsf", "Charlie"
    "2234fsw", "Robert"
```

test step configuration may look like this:

``` xml
    <csv name="csvTestStep" fileName="data.csv">
        <param name="id" value="0"/>
        <param name="name" value="1"/>
    </csv>
```

after running this step for the first time, other steps can access data like this:

``` xml
    <rest name="setup" endpoint="/user" method="post">
        <param name="id" value="${testCase.testSteps.csvTestStep.properties.deviceId}"/>
        <param name="username" value="${testCase.testSteps.csvTestStep.properties.name"/>
    </rest>
```

CSV test step can be used with Loop test step in order to cycle through all rows. You can also use a groovy test step to run CSV step in order to read another line.

#### Loop

This step can be used in two ways:
1. as a loop through rows of a CSV step
2. as a loop of a series of steps

Step specific attributes:
* dataSource - optional. Define name of the data source step. Currently supports only CSV steps.
* target - required. Name of the test step to go to after each loop.
* repeat - optional. How many times repeat the loop. Required when `ds` is not defined.

``` xml
    <loop name="loopTestStep" dataSource="csvTestStep" target="setup"/>
```

Logging
-------

We use (Logback)[http://logback.qos.ch] and logback.xml file to configure logging. Sample file is included in the project. Feel free to modify it to suit your needs.
Note the `XML`, `JSON` and `Statistics` loggers.

Limitations
-----------

There are plenty of limitations, simply because we have never meant this to be released, rather wanted to have something to test our APIs easily.
After we've spent some time with it, we decided resTest might come in handy to others. Missing features described below will be implemented in near future.

So for now:
* basic running options. Although we are prepared for some fancy test case execution control, we didn't have time to implement any "console" or "interface" for it. Java Curses is the only thing we have found but it's dependencies are a killer.
* no built in load testing. This is going to be our primary goal now. In order to achieve load test simply edit resTest.groovy, load test case as many times as you want and run them all at the same time. Unfortunately this will mean problems with CSV test step.
* no reporting. Well almost. You do get a nice table with AVG/MIN/MAX response times. We simply didn't need any reports apart from this list.
* poor documentation. We will try to improve it over time.

Usage
-----

Use whatever you want to create your Test Case XML file (we've included `testCaseSchema.xsd` file which should help a bit). We strongly recommend [IntelliJ IDEA](http://http://www.jetbrains.com/idea/features/xml_editor.html).

We don't have a proper loader yet, but you can run your test case like this:

`groovy -cp src resTest.groovy testCases/someTestCase.xml`


Contributing
------------

1. Fork it.
2. Create a branch (`git checkout -b my_markup`)
3. Commit your changes (`git commit -am "Added Snarkdown"`)
4. Push to the branch (`git push origin my_markup`)
5. Create a pull request.
