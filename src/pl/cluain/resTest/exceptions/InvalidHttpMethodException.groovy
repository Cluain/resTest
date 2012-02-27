/*
 * Copyright 2012 Cluain Krystian Szczesny
 *
 * InvalidHttpMethodException.groovy is part of resTest.
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



package pl.cluain.resTest.exceptions

import groovy.util.logging.Slf4j

@Slf4j
class InvalidHttpMethodException extends Exception {

    public InvalidHttpMethodException() {
        super()
        log.error("Supported HTTP methods are: get, post, put, delete, head, options")
    }

    public InvalidHttpMethodException(String msg) {
        super(msg)
        log.error("Supported HTTP methods are: get, post, put, delete, head, options")
    }
}
