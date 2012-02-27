/*
 * Copyright 2012 Cluain Krystian Szczesny
 *
 * resTest.groovy is part of resTest.
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

import pl.cluain.resTest.TestCaseLoader

def main() {

    TestCaseLoader.loadTestCase(args[0]).runTestCase()

}

main()
