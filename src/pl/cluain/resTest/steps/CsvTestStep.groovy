/*
 * Copyright 2012 Cluain Krystian Szczesny
 *
 * CsvTestStep.groovy is part of resTest.
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

import au.com.bytecode.opencsv.CSVReader
import groovy.util.logging.Slf4j
import pl.cluain.resTest.TestCase
import pl.cluain.resTest.TestStep

@Slf4j
class CsvTestStep implements TestStep {

    String name
    long duration
    Map params = [:]
    Map properties = [:]
    TestCase testCase
    String lineSeparator = ','
    BufferedReader csvData
    boolean ready

    public CsvTestStep(String name, def params, TestCase testCase, String filename, String lineSeparator) {

        this.testCase = testCase
        this.name = name
        this.params = params
        lineSeparator != null ? this.lineSeparator = lineSeparator : null

        this.csvData = new BufferedReader(new FileReader(filename))

    }

    void runStep() {
        log.debug("[{}] Run Step CSV_STEP", name)
        long start = System.currentTimeMillis()

        CSVReader reader = new CSVReader(new StringReader(this.csvData.readLine()), this.lineSeparator.toCharacter());
        String[] nextLine;
        def record = reader.readNext()

        params.each {
            properties[it.key] = record[Integer.parseInt((String) it.value)]
        }

        ready = this.csvData.ready()

        duration = duration = System.currentTimeMillis() - start
        log.debug("[{}] Run Step CSV_STEP end [{}ms]", name, duration)
    }

}
