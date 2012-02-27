/*
 * Copyright 2012 Cluain Krystian Szczesny
 *
 * RestTestStep.groovy is part of resTest.
 *
 * resTest is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * resTest is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with resTest.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.cluain.resTest.steps

import groovy.util.logging.Slf4j
import groovyx.net.http.RESTClient
import net.sf.json.groovy.JsonSlurper
import pl.cluain.resTest.RestResponseType
import pl.cluain.resTest.TestCase
import pl.cluain.resTest.TestStep
import pl.cluain.resTest.exceptions.InvalidHttpMethodException
import pl.cluain.resTest.exceptions.ValidationFailedException
import pl.cluain.resTest.log.JSON
import pl.cluain.resTest.log.XML
import static groovyx.net.http.ContentType.TEXT

@Slf4j
class RestTestStep implements TestStep {

    String name
    long duration
    Map<String, String> params = [:]
    Map<String, Script> originalParams = [:]
    Map properties = [:]
    def xmlResponse
    def jsonResponse
    String status
    boolean valid
    TestCase testCase
    String url
    String endpoint
    String method
    boolean logResponse
    RestResponseType responseType

    Script beforeTestScript
    Script validateScript
    Script afterTestScript

    RESTClient http

    public RestTestStep(String name, String url, String endpoint, String method, Map<String, String> params, String beforeTestScriptString, String validateScriptString, String afterTestScriptString, TestCase testCase, String logResponse, String responseType) {
        Binding binding = new Binding()
        binding.setVariable("log", log)
        GroovyShell shell = new GroovyShell(binding)
        shell.setVariable("testCase", testCase)
        shell.setVariable("current", this)

        this.testCase = testCase
        this.name = name
        this.url = url
        this.endpoint = endpoint
        this.method = method && method != "" ? method : "get"
        checkMethod()
        this.logResponse = logResponse && logResponse != "" ? Boolean.parseBoolean(logResponse) : false
        if (responseType == null || responseType == "") {
            this.responseType = RestResponseType.XML
        } else if (responseType == RestResponseType.XML.value()) {
            this.responseType = RestResponseType.XML
        } else {
            this.responseType = RestResponseType.JSON
        }



        params.each {
            originalParams.put(it.key, shell.parse("\"" + it.value + "\""))
        }

        this.beforeTestScript = beforeTestScriptString && beforeTestScriptString != "" ? shell.parse(beforeTestScriptString) : null
        boolean sharedValidation = testCase.sharedRestValidation && testCase.sharedRestValidation
        if (sharedValidation) {
            if (validateScriptString && validateScriptString != "") {
                this.validateScript = shell.parse(testCase.sharedRestValidation + "\n" + validateScriptString + "\n return true")
            } else {
                this.validateScript = shell.parse(testCase.sharedRestValidation + "\n return true")
            }
        } else if (validateScriptString && validateScriptString != "") {
            this.validateScript = shell.parse(validateScriptString + "\n return true")
        }
        this.afterTestScript = afterTestScriptString && afterTestScriptString != "" ? shell.parse(afterTestScriptString) : null
        this.http = new RESTClient(url, TEXT)

        if (!testCase.statistics[name]) testCase.statistics[name] = []

    }

    private void beforeTest() {
        log.debug("[{}] Before Test", name)
        long start = System.currentTimeMillis()

        if (beforeTestScript) beforeTestScript.run()

        log.debug("[{}] Before Test end [{}ms]", name, System.currentTimeMillis() - start)
    }

    void runStep() {

        beforeTest()

        runRestStep()

        if (!validate()) {
            throw new ValidationFailedException("$name validation failed")
        }

        afterTest()
    }

    private void runRestStep() {
        log.debug("[{}] Run Step REST_STEP", name)

        originalParams.each {
            params.put(it.key, (String) it.value.run())
        }

        log.debug("{}{} params {}", url, endpoint, params)
        long startRequest = System.currentTimeMillis()
        def resp = http."get"(path: endpoint, query: params, headers: [Accept: "text/xml;charset=UTF-8"])

        duration = System.currentTimeMillis() - startRequest
        status = null
        status = resp.status
        String rawResponse = resp.data.text
        if (responseType == RestResponseType.XML) {
            xmlResponse = new XmlParser().parseText(rawResponse)
            XML.print(name, rawResponse, logResponse)
        } else {
            jsonResponse = new JsonSlurper().parse(rawResponse)
            JSON.print(name, rawResponse, logResponse)
        }

        log.debug("[{}] Run Step REST_STEP end [{}ms]", name, duration)
    }

    private boolean validate() {
        log.debug("[{}] Validate", name)
        long start = System.currentTimeMillis()
        valid = true


        if (validateScript) {
            if (!validateScript.run()) {
                valid = false
            }
        }

        testCase.statistics[name].add(duration)

        log.debug("[{}] Validate end [{}ms]", name, System.currentTimeMillis() - start)
        return valid
    }

    private void afterTest() {
        log.debug("[{}] After Test", name)
        long start = System.currentTimeMillis()

        if (afterTestScript) afterTestScript.run()

        log.debug("[{}] After Test end [{}ms]", name, System.currentTimeMillis() - start)
    }

    private void checkMethod() {
        if (!(["get", "post", "delete", "head", "options", "put"].contains(method))) {
            throw new InvalidHttpMethodException(method)
        }
    }


    def getResponse() {
        def obj
        switch (responseType) {
            case RestResponseType.XML:
                obj = xmlResponse
                break
            case RestResponseType.JSON:
                obj = jsonResponse
                break
        }
        return obj
    }
}
