/*
 * Copyright 2012 Cluain Krystian Szczesny
 *
 * TestCaseImpl.groovy is part of resTest.
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

package pl.cluain.resTest

import groovy.util.logging.Slf4j
import groovyx.net.http.ParserRegistry
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import pl.cluain.resTest.log.Statistics
import pl.cluain.resTest.steps.CsvTestStep
import pl.cluain.resTest.steps.GroovyTestStep
import pl.cluain.resTest.steps.LoopTestStep
import pl.cluain.resTest.steps.RestTestStep

@Slf4j
class TestCase {

    Map<String, TestStep> testSteps = [:]
    List<String> orderOfSteps = []
    int currentTestStepNumber = 0
    String url
    String name
    String sharedRestValidation
    Map props = [:]
    Map<String, List<Long>> statistics = [:]

    //control
    TestStepRunnable currentTestStep
    ExecutorService threadPool = Executors.newCachedThreadPool()
    //Thread currentTestStepThread
    boolean pause = false
    boolean stop = false
    boolean running = false // indicates whether whole test case is ran

    public TestCase() {
        ParserRegistry.setDefaultCharset("UTF-8")
    }

    void addGroovyTestStep(Node testStep) {
        testSteps.put(testStep.'@name', new GroovyTestStep(testStep.'@name', testStep.text(), this))
        log.debug("Added GROOVY_STEP test step {}", testStep.'@name')
    }

    void addRestTestStep(Node testStep) {
        def parameters = [:]
        testStep.param.each {
            parameters.put(it.'@name', it.'@value')
        }
        testSteps.put(testStep.'@name', new RestTestStep(testStep.'@name', testStep.'@url' ? testStep.'@url' : url, testStep.'@endpoint', testStep.'@method', parameters,
                testStep.beforeTest.text(), testStep.validate.text(), testStep.afterTest.text(), this, testStep.'@logResponse', testStep.'@responseType'))

        log.debug("Added REST_STEP test step {}", testStep.'@name')
    }

    void addCsvTestStep(Node testStep) {
        def parameters = [:]
        testStep.param.each {
            parameters.put(it.'@name', it.'@value')
        }
        testSteps.put(testStep.'@name', new CsvTestStep((String) testStep.'@name', parameters, this, (String) testStep.'@fileName', (String) testStep.'@lineseparator'))

        log.debug("Added CSV_STEP test step {}", testStep.'@name')
    }

    void addLoopTestStep(Node testStep) {
        testSteps.put(testStep.'@name', new LoopTestStep((String) testStep.'@name', (TestCase) this, (String) testStep.'@target', (String) testStep.'@dataSource', (String) testStep.'@repeat'))

        log.debug("Added LOOP_STEP test step {}", testStep.'@name')
    }

    void runTestCase() {
        currentTestStepNumber = 0
        running = true
        stop = false
        pause = false

        log.debug("[{}] Starting Test Case", name)

        runNextTestStep()

    }

    void runNextTestStep() {
        log.debug("[{}] Starting test step {}", name, currentTestStepNumber)

        TestStep step = testSteps.get(orderOfSteps[currentTestStepNumber])
        currentTestStep = new TestStepRunnable(step, this)

        threadPool.execute(currentTestStep)

    }

    void runTestStep(String testName) {
        log.debug("[{}] Starting test step {}", name, testName)

        TestStep step = testSteps[testName]
        step.runStep()
    }

    void goToTestStep(String testName) {
        log.debug("[{}] Going to test step {}", name, testName)

        currentTestStepNumber = orderOfSteps.findIndexOf {it == testName} - 1
    }

    void testStepFinished() {
        log.debug("Test step [{}] finished", currentTestStepNumber)
        currentTestStep = null
        //currentTestStepThread = null
        currentTestStepNumber++

        if (currentTestStepNumber >= orderOfSteps.size()) {
            running = false
        }

        if (running && pause) while (running && pause) sleep(100)

        if (stop) cleanup()

        if (running) {
            runNextTestStep()
        } else {
            log.debug("[{}] Test Case Finished", name)
            cleanup()
        }

    }

    void pause() {
        pause = true;
        if (currentTestStep) currentTestStep.pause()
        log.debug("[{}] Test case paused", name)
    }

    void stop() {
        stop = true
        running = false
        pause = false

        currentTestStep.stop()

        log.debug("[{}] Test case stopped", name)
    }

    void resume() {
        pause = false;
        if (currentTestStep) currentTestStep.resume()
        log.debug("[{}] Test case resumed", name)
    }

    void cleanup() {
        Statistics.print(statistics)
        threadPool.shutdown()
        log.debug("[{}] Test cleaned up", name)
    }

    void controlCheck() {
        if (pause) while (pause && !stop) Thread.wait(500)
        if (stop) Thread.currentThread().interrupt();
    }

}
