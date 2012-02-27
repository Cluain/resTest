/*
 * Copyright 2012 Cluain Krystian Szczesny
 *
 * LoopTestStep.groovy is part of resTest.
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
import pl.cluain.resTest.TestCase
import pl.cluain.resTest.TestStep

@Slf4j
class LoopTestStep implements TestStep {

    String name
    long duration
    Map properties = [:]
    TestCase testCase
    String targetStep
    String dataSource
    Integer repeat
    int goToStepNumber
    int stepLoopCount = 0

    public LoopTestStep(String name, TestCase testCase, String target, String dataSource, String repeat) {

        this.testCase = testCase
        this.name = name
        this.targetStep = target
        this.dataSource = dataSource != null ? this.dataSource = dataSource : null
        this.repeat = repeat != null ? Integer.parseInt(repeat) : null
    }

    void runStep() {
        log.debug("[{}] Run Step LOOP_STEP", name)
        long start = System.currentTimeMillis()

        //in case this step is CSV_STEP we only check if next line is readable and loop back to target
        if (dataSource) {
            if (((CsvTestStep) testCase.testSteps[dataSource]).csvData.ready()) {
                if (!goToStepNumber) {
                    goToStepNumber = testCase.orderOfSteps.findIndexOf {it == targetStep}
                }
                testCase.currentTestStepNumber = goToStepNumber - 1
                testCase.runTestStep(dataSource)
            }
        } else {
            if (repeat && (stepLoopCount + 1 <= repeat)) {
                stepLoopCount++
                if (!goToStepNumber) {
                    goToStepNumber = testCase.orderOfSteps.findIndexOf {it == targetStep}
                }
                testCase.currentTestStepNumber = goToStepNumber - 1
                log.info("[{}] LOOP_STEP: {}", name, stepLoopCount)
            }
            else {
                log.error("[{}] LOOP_STEP: No condition was met. Loop Count: {} Repeat: {}", name, stepLoopCount, repeat)
            }
        }
        duration = duration = System.currentTimeMillis() - start
        log.debug("[{}] Run Step LOOP_STEP end [{}ms]", name, duration)
    }

}
