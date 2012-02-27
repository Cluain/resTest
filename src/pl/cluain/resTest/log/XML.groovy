/*
 * Copyright 2012 Cluain Krystian Szczesny
 *
 * XML.groovy is part of resTest.
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
class XML {
    public static void print(String apiName, String xml, boolean logResponse) {
        if (logResponse) {
            pl.cluain.resTest.log.XML.log.info("[{}] Response: \n{}", apiName, xml)
        } else {
            pl.cluain.resTest.log.XML.log.debug("[{}] Response: \n{}", apiName, xml)
        }
    }
}
