/*
 * Copyright 2012 Cluain Krystian Szczesny
 *
 * TestCaseLoader.groovy is part of resTest.
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



@Grab(group = 'org.slf4j', module = 'slf4j-api', version = '1.6.4')
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.0.0')
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.5.2')
@Grab('com.xlson.groovycsv:groovycsv:0.2')

package pl.cluain.resTest


import groovy.util.logging.Slf4j

@Slf4j
class TestCaseLoader {
    static TestCase loadTestCase(String file) {
        TestCase testCase = new TestCase()

        Node testCaseXml = new XmlParser().parse(file)

        testCase.url = testCaseXml.'@url'
        testCase.name = testCaseXml.'@name'
        testCase.sharedRestValidation = testCaseXml.sharedRestValidation.text()

        testCaseXml.each {
            switch (it.name().localPart) {
                case TestCaseEntry.PROPERTY.value():
                    testCase.props.put(it.'@name', it.'@value')
                    break
                case TestCaseEntry.GROOVY_STEP.value():
                    testCase.orderOfSteps.add(it.'@name')
                    testCase.addGroovyTestStep(it)
                    break
                case TestCaseEntry.REST_STEP.value():
                    testCase.orderOfSteps.add(it.'@name')
                    testCase.addRestTestStep(it)
                    break
                case TestCaseEntry.CSV_STEP.value():
                    testCase.orderOfSteps.add(it.'@name')
                    testCase.addCsvTestStep(it)
                    break
                case TestCaseEntry.LOOP_STEP.value():
                    testCase.orderOfSteps.add(it.'@name')
                    testCase.addLoopTestStep(it)
                    break
            }
        }

        log.debug("[{}] Test case loaded from {}", testCase.name, file)
        return testCase
    }
}
