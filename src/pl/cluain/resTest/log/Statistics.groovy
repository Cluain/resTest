/*
 * Copyright 2012 Cluain Krystian Szczesny
 *
 * Statistics.groovy is part of resTest.
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

package pl.cluain.resTest.log

import groovy.util.logging.Slf4j

@Slf4j
class Statistics {
    public static void print(Map<String, List<Long>> stats) {
        log.info("".padRight(80, "-"))
        log.info("|{}|", "RESULTS".center(78, " "))
        log.info("".padRight(80, "-"))
        stats.each {
            if (it.value.size() > 10) {
                it.value.remove(it.value.max())
                it.value.remove(it.value.max())
                it.value.remove(it.value.min())
                it.value.remove(it.value.min())
            }
            log.debug("{}: {}", it.key, it.value)
            if (it.value.size() > 1) {
                log.info("| {} AVG: {}  MAX: {}  MIN: {}  |", it.key.padRight(31, " "), (((int) it.value.sum() / it.value.size())).toString().padRight(8, " "), it.value.max().toString().padRight(8, " "), it.value.min().toString().padRight(8, " "))
            }else if(it.value.size() == 1){
                log.info("| {} AVG: {}  MAX: {}  MIN: {}  |", it.key.padRight(31, " "), (((int) it.value[0])).toString().padRight(8, " "), it.value.max().toString().padRight(8, " "), it.value.min().toString().padRight(8, " "))
            }
        }
        log.info("".padRight(80, "-"))
    }
}
