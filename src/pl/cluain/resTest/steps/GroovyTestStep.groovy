/*
 * Copyright 2012 Cluain Krystian Szczesny
 *
 * GroovyTestStep.groovy is part of resTest.
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
import groovy.util.slurpersupport.NodeChild
import pl.cluain.resTest.TestCase
import pl.cluain.resTest.TestStep

@Slf4j
class GroovyTestStep implements TestStep {

    long duration
    String name
    Map properties = [:]
    TestCase testCase
    Script groovyScript
    NodeChild response
    String status

    public GroovyTestStep(String name, String groovyScriptString, TestCase testCase) {
        Binding binding = new Binding()
        binding.setVariable("log", log)
        GroovyShell shell = new GroovyShell(binding)
        shell.setVariable("testCase", testCase)
        shell.setVariable("current", this)

        this.testCase = testCase
        this.name = name
        this.groovyScript = groovyScriptString != "" ? shell.parse(groovyScriptString) : null

    }

    void runStep() {
        log.debug("[{}] Run Step Groovy", name)
        long start = System.currentTimeMillis()

        groovyScript.run()

        duration = duration = System.currentTimeMillis() - start
        log.debug("[{}] Run Step Groovy end [{}ms]", name, duration)
    }

}
