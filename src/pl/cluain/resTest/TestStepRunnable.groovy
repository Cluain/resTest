/*
 * Copyright 2012 Cluain Krystian Szczesny
 *
 * TestStepRunnable.groovy is part of resTest.
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
import pl.cluain.resTest.exceptions.UnsuccessfulTestStepException
import pl.cluain.resTest.exceptions.ValidationFailedException

@Slf4j
class TestStepRunnable implements Runnable {

    TestStep step;
    TestCase testCase;
    boolean pause
    boolean stop
    Thread currentThread;

    public TestStepRunnable(TestStep testStep, TestCase testCase) {
        this.step = testStep
        this.testCase = testCase
    }

    void run() {
        try {
            currentThread = Thread.currentThread();
            log.debug("running {}", step.name)
            step.runStep()
            if (!checkCondition()) return
            testCase.testStepFinished()
        } catch (InterruptedException e) {
            testCase.cleanup()
            log.info("[{}] Test Step stopped", step.getName())
        } catch (ValidationFailedException e) {
            log.error("Validation failed", e)
            testCase.stop()
            testCase.cleanup()
        } catch (UnsuccessfulTestStepException e) {
            log.error("Test step was not successful", e)
            testCase.stop()
            testCase.cleanup()
        }
    }

    void stop() {
        log.debug("Stopping {}", step.name)
        stop = true
    }

    void pause() {
        log.debug("Pausing {}", step.name)
        pause = true
    }

    void resume() {
        log.debug("Resuming {}", step.name)
        pause = false
    }

    boolean checkCondition() {
        if (pause) while (pause) sleep(100)
        if (stop) return false
        return true
    }

}
